package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiClientFactory;
import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.BinanceApiWebSocketClient;
import com.gant.binance.api.client.domain.event.CandlestickEvent;
import com.gant.binance.api.client.domain.market.CandlestickInterval;
import com.gant.trade.config.BotConfig;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.model.Timeframe;
import com.gant.trade.model.TradeDirection;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.mongo.service.UserService;
import com.gant.trade.rest.model.*;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.service.TradeStrategyService;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.BarSeriesUtil;
import com.gant.trade.utility.SymbolInfoUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class TradeStrategyBinanceService implements TradeStrategyService {

    private final BotConfig botConfig;
    private final TradeService tradeService;
    private final OrderBinanceService orderBinanceService;
    private final TelegramBotService telegramBotService;
    private final HistoricalCandlesBinanceService historicalCandlesBinanceService;
    private final SymbolInfoUtil symbolInfoUtil;
    private final UserService userService;

    private BinanceApiRestClient binanceApiRestClient;
    private BinanceApiWebSocketClient binanceApiWebSocketClient;

    private StrategyTO strategyTO;
    private Timeframe timeframe;
    private Map<String, BarMerger> candleMerger;
    private Map<String, BarSeries> barSeries;
    private Map<String, LocalDateTime> candlestickEventLastTimes;
    private Map<String, Strategy> strategies;
    private List<SymbolInfo> currencies;
    private Map<String, List<Trade>> trades;
    private List<Closeable> candlestickSubscriptions;
    private Map<String, TradingRecord> tradingRecordMap;

    private final boolean debug;
    private int maxRetry = 0;

    //TODO perchè non usare autowired?
    public TradeStrategyBinanceService(ApplicationContext applicationContext, Boolean debug) {
        this.botConfig = applicationContext.getBean(BotConfig.class);
        this.tradeService = applicationContext.getBean(TradeService.class);
        this.orderBinanceService = applicationContext.getBean(OrderBinanceService.class);
        this.telegramBotService = applicationContext.getBean(TelegramBotService.class);
        this.historicalCandlesBinanceService = applicationContext.getBean(HistoricalCandlesBinanceService.class);
        this.symbolInfoUtil = applicationContext.getBean(SymbolInfoUtil.class);
        this.userService = applicationContext.getBean(UserService.class);
        this.debug = debug;
    }

    public boolean status() {
        return candlestickEventLastTimes.values().stream()
                .allMatch(candlestickEventLastTime -> Duration.between(candlestickEventLastTime, LocalDateTime.now()).toMinutes() < botConfig.getHealthCheck());
    }

    public boolean stop() {
        AtomicBoolean stopped = new AtomicBoolean(false);
        if (candlestickSubscriptions != null) {
            stopped.set(true);
            candlestickSubscriptions.forEach(closeable -> {
                try {
                    closeable.close();
                } catch (IOException e) {
                    stopped.set(false);
                    log.error("candlestickSubscriptions", e);
                }
            });
            candlestickEventLastTimes = null;
        }

        return stopped.get();
    }

    public void start(StrategyTO strategyTO, User user) {
        try {
            MDC.put("StrategyName", strategyTO.getName());
            if (binanceApiRestClient == null && binanceApiWebSocketClient == null) {
                ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, Exchange.BINANCE);
                if (userExchange != null) {
                    BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance(userExchange.getApiKey(), userExchange.getSecretKey());
                    binanceApiRestClient = binanceApiClientFactory.newRestClient();
                    binanceApiWebSocketClient = binanceApiClientFactory.newWebSocketClient();
                }
            }

            this.strategyTO = strategyTO;
            this.timeframe = Timeframe.getTimeframe(strategyTO.getTimeframe().toString());
            this.candleMerger = new HashMap<>();
            this.candlestickEventLastTimes = new HashMap<>();
            this.currencies = strategyTO.getSymbolInfo().stream().map(currency ->
                            symbolInfoUtil.getSymbolInfoByExchange(strategyTO.getExchange(), strategyTO.getUserId(), currency.getSymbol(), currency.getOrderSize().doubleValue()))
                    .collect(Collectors.toList());

            this.barSeries = new HashMap<>();
            historicalCandlesBinanceService.requestHistoricalCandles(binanceApiRestClient, timeframe, currencies).forEach((k, v) -> barSeries.put(k.getSymbolInfo(), v));

            this.trades = this.tradeService.getOpenTradesByStrategy(strategyTO.getSeqId());
            //TODO creare tradingRecordMap in base all'exchange?
            this.tradingRecordMap = TradeStrategyServiceUtil.getTradingRecordMap(currencies, trades, barSeries, strategyTO.getName());

            this.strategies = TradeStrategyServiceUtil.getStrategies(currencies, barSeries, strategyTO);

            registerCandles();
            maxRetry = 0;
        } catch (Exception e) {
            log.error("{} Got exception", strategyTO.getName(), e);

            try {
                if (botConfig.getMaxRetry() > maxRetry) {
                    TimeUnit.SECONDS.sleep(botConfig.getRetryInSecond());
                    maxRetry++;
                    if (user != null) {
                        telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), strategyTO.getName() + " Got exception: " + e.getMessage());
                    }
                    start(strategyTO, user);
                } else if (user != null) {
                    telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), "Error with starting strategy " + strategyTO.getName() + "\n" + e.getMessage());
                }
            } catch (InterruptedException ie) {
                log.error("{} Got InterruptedException", strategyTO.getName(), ie);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void registerCandles() {
        log.info("{} Register candles", strategyTO.getName());
        candlestickSubscriptions = currencies.stream().map(currency -> {
                    candleMerger.put(currency.getSymbol(), new BarMerger(currency, timeframe, strategyTO.getCheckRulesEveryTime(), strategyTO.getCheckRulesEveryTimeValue(), this::barDoneCallback));

                    return binanceApiWebSocketClient.onCandlestickEvent(
                            currency.getSymbol().toLowerCase(),
                            CandlestickInterval.valueOf(timeframe.name()),
                            candlestickEvent -> handleCandlestickCallback(currency, candlestickEvent)
                    );
                })
                .collect(Collectors.toList());
    }

    public void barDoneCallback(SymbolInfo symbolInfo, final Bar bar) {
        try {
            BarSeriesUtil.addBar(barSeries, symbolInfo, bar, strategyTO.getName());
            shouldEnterExit(symbolInfo, bar);
        } catch (Exception e) {
            log.error("Got an exception while executing order", e);
            UserTO user = userService.getUserById(strategyTO.getUserId());
            if (user != null) {
                telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), "Got an exception while executing order: " + e.getMessage());
            }
        }
    }

    public void shouldEnterExit(SymbolInfo symbolInfo, final Bar bar) {
        BarSeries bs = barSeries.get(symbolInfo.getSymbol());
        final int endIndex = bs.getEndIndex();
        final Trade openTrade = TradeStrategyServiceUtil.getOpenTrade(symbolInfo, trades, strategyTO.getName());
        TradingRecord tradingRecord = tradingRecordMap.get(symbolInfo.getSymbol());
        log.debug("{} symbolInfo={} price={} openTrade={} price={}", strategyTO.getName(), symbolInfo.getSymbol(), bar.getClosePrice(), openTrade != null, openTrade != null ? openTrade.getExpectedPriceOpen() : 0);
        if (openTrade == null && strategies.get(symbolInfo.getSymbol()).shouldEnter(endIndex)) {
            openOrder(symbolInfo, bar);
            boolean enter = tradingRecord.enter(endIndex, bar.getClosePrice(), DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo)));
            log.info("Order enter = {}", enter);
        } else if (openTrade != null && strategies.get(symbolInfo.getSymbol()).shouldExit(endIndex, tradingRecord)) {
            closeOrder(symbolInfo, bar, openTrade);
            boolean exit = tradingRecord.exit(endIndex, bar.getClosePrice(), DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo)));
            log.info("Order exit = {}", exit);
        }
    }

    public void openOrder(SymbolInfo symbolInfo, final Bar bar) {
        log.info("{} openOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        Trade trade = new Trade(strategyTO.getSeqId(), strategyTO.getUserId(), Exchange.BINANCE, TradeDirection.LONG.name(), symbolInfo.getSymbol(), symbolInfo.getOrderSize());
        tradeService.addTradeToOpenTradeList(trades, trade, symbolInfo.getOrderSize());
        orderBinanceService.openTrade(binanceApiRestClient, trade, bar, symbolInfo.getOrderSize(), debug);
    }

    public void closeOrder(SymbolInfo symbolInfo, final Bar bar, final Trade openTrade) {
        log.info("{} closeOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        orderBinanceService.closeTrade(binanceApiRestClient, openTrade, bar, symbolInfo.getOrderSize(), debug);
        tradeService.removeTradeToOpenTradeList(trades, openTrade);
    }

    public void closeOrderManually(SymbolInfo symbolInfo, final Trade openTrade) {
        Bar bar = barSeries.get(symbolInfo.getSymbol()).getLastBar();
        closeOrder(symbolInfo, bar, openTrade);
    }

    public void handleCandlestickCallback(SymbolInfo symbolInfo, final CandlestickEvent candlestickEvent) {
        MDC.put("StrategyName", strategyTO.getName());
        log.trace("{} currencyPair={} price={}", strategyTO.getName(), symbolInfo.getSymbol(), candlestickEvent.getClose());
        candlestickEventLastTimes.put(symbolInfo.getSymbol(), LocalDateTime.now());
        candleMerger.get(symbolInfo.getSymbol()).addNewPrice(
                candlestickEvent.getEventTime(),
                new BigDecimal(candlestickEvent.getOpen()),
                new BigDecimal(candlestickEvent.getClose()),
                new BigDecimal(candlestickEvent.getVolume()),
                candlestickEvent.getBarFinal()
        );
    }

    public Map<String, LocalDateTime> getCandlestickEventLastTimes() {
        return candlestickEventLastTimes;
    }

    public StrategyTO getStrategyTO() {
        return strategyTO;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public List<StrategyStatusInfoTO> getStrategyStatusInfoToList() {
        return currencies.stream().map(symbolInfo -> {
            StrategyStatusInfoTO strategyStatusInfoTO = new StrategyStatusInfoTO();
            String price = binanceApiRestClient.getPrice(symbolInfo.getSymbol()).getPrice();
            strategyStatusInfoTO.setPrice(Double.parseDouble(price));
            strategyStatusInfoTO.setRsi(new ArrayList<>());
            BarSeries bs = barSeries.get(symbolInfo.getSymbol());
            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(bs);
            List<IndicatorTO> rsiIndicators = strategyTO.getRules().stream()
                    .map(ruleTO -> ruleTO.getCondition().getIndicators().stream()
                            .filter(indicatorTO -> IndicatorTypeEnum.RSI.equals(indicatorTO.getType()) && indicatorTO.getIndicatorTypeRef() == null)
                            .collect(Collectors.toList())).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            for (IndicatorTO indicatorTO : rsiIndicators) {
                RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
                RSITO rsiTO = new RSITO();
                rsiTO.setPeriod(indicatorTO.getPeriod().intValue());
                rsiTO.setValue(rsiIndicator.getValue(bs.getEndIndex()).doubleValue());
                strategyStatusInfoTO.getRsi().add(rsiTO);
            }
            if (strategyStatusInfoTO.getRsi().isEmpty()) {
                RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
                RSITO rsiTO = new RSITO();
                rsiTO.setPeriod(14);
                rsiTO.setValue(rsiIndicator.getValue(bs.getEndIndex()).doubleValue());
                strategyStatusInfoTO.getRsi().add(rsiTO);
            }
            VolumeIndicator volumeIndicator = new VolumeIndicator(bs, 20); // TODO add volume to IndicatorTypeEnum
            strategyStatusInfoTO.setVolume(volumeIndicator.getValue(bs.getEndIndex()).doubleValue());

            strategyStatusInfoTO.setOrders(tradeService.getOpenOrderByCurrencyAndStrategySeqId(symbolInfo.getSymbol(), strategyTO.getSeqId()));

            return strategyStatusInfoTO;
        }).collect(Collectors.toList());
    }
}
