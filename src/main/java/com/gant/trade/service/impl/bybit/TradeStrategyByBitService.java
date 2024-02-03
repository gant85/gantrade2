package com.gant.trade.service.impl.bybit;

import com.gant.trade.config.BotConfig;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.model.CandlestickEvent;
import com.gant.trade.model.Timeframe;
import com.gant.trade.model.TradeDirection;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.mongo.service.UserService;
import com.gant.trade.proxy.bybit.v5.ByBitProxy;
import com.gant.trade.rest.model.*;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.service.TradeStrategyService;
import com.gant.trade.service.impl.binance.StatusInfoService;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.BarSeriesUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.impl.bybit.ByBitSymbolInfoUtil;
import com.gant.trade.websocket.bybit.ByBitWebSocketClient;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class TradeStrategyByBitService implements TradeStrategyService {

    private final BotConfig botConfig;
    private final TradeService tradeService;
    private final OrderByBitService orderByBitService;
    private final TelegramBotService telegramBotService;
    private ByBitProxy byBitProxy;
    private ByBitWebSocketClient byBitWebSocketClient;
    private final HistoricalCandlesByBitService historicalCandlesByBitService;
    private final ByBitSymbolInfoUtil symbolInfoUtil;
    private final UserService userService;
    private final StatusInfoService statusInfoService;
    private StrategyTO strategyTO;
    private Timeframe timeframe;
    private Map<String, BarMerger> candleMerger;
    private Map<String, BarSeries> barSeries;
    private Map<String, LocalDateTime> candlestickEventLastTimes;
    private Map<String, Strategy> strategies;
    private List<SymbolInfo> currencies;
    private Map<String, List<Trade>> trades;
    private Disposable klinesSubscriber;
    private Map<String, TradingRecord> tradingRecordMap;
    private User user;
    private final boolean debug;
    private int maxRetry = 0;
    private final ApplicationContext applicationContext;

    public TradeStrategyByBitService(ApplicationContext applicationContext, Boolean debug) {
        this.applicationContext = applicationContext;
        this.botConfig = applicationContext.getBean(BotConfig.class);
        this.tradeService = applicationContext.getBean(TradeService.class);
        this.orderByBitService = applicationContext.getBean(OrderByBitService.class);
        this.telegramBotService = applicationContext.getBean(TelegramBotService.class);
        this.historicalCandlesByBitService = applicationContext.getBean(HistoricalCandlesByBitService.class);
        this.symbolInfoUtil = new ByBitSymbolInfoUtil(applicationContext);
        this.userService = applicationContext.getBean(UserService.class);
        this.statusInfoService = applicationContext.getBean(StatusInfoService.class);
        this.debug = debug;
    }

    @Override
    public boolean status() {
        return false;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public void start(StrategyTO strategyTO, User user) {
        try {
            MDC.put("StrategyName", strategyTO.getName());
            this.strategyTO = strategyTO;
            this.timeframe = Timeframe.getTimeframe(strategyTO.getTimeframe().toString());

            ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, Exchange.BYBIT);
            if (userExchange != null) {
                this.byBitProxy = new ByBitProxy(applicationContext, userExchange);
                this.byBitWebSocketClient = new ByBitWebSocketClient(botConfig.getExchange().getBybit().getWebSocket().getBasePath(), byBitProxy.getBybitEncryption());
                this.byBitWebSocketClient.connect();
            }

            this.candleMerger = new HashMap<>();
            this.candlestickEventLastTimes = new HashMap<>();
            this.currencies = strategyTO.getSymbolInfo().stream().map(currency ->
                            symbolInfoUtil.getSymbolInfoByExchange(byBitProxy, strategyTO.getExchange(), strategyTO.getUserId(), currency.getSymbol(), currency.getOrderSize().doubleValue()))
                    .collect(Collectors.toList());

            this.barSeries = new HashMap<>();
            Long startTime = LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            historicalCandlesByBitService.requestHistoricalCandles(byBitProxy, timeframe, currencies, startTime, endTime).forEach((k, v) -> barSeries.put(k.getSymbolInfo(), v));

            this.trades = this.tradeService.getOpenTradesByStrategy(strategyTO.getSeqId());
            this.tradingRecordMap = TradeStrategyServiceUtil.getTradingRecordMap(currencies, trades, barSeries, strategyTO.getName());

            this.strategies = TradeStrategyServiceUtil.getStrategies(currencies, barSeries, strategyTO);
            this.user = user;

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

    @Override
    public void registerCandles() throws IOException {
        log.info("{} Register candles", strategyTO.getName());
        klinesSubscriber = byBitWebSocketClient.subscribeKlines(currencies, timeframe)
                .subscribe(candlestickEvent -> {
                    for (SymbolInfo symbolInfo : currencies) {
                        candleMerger.put(symbolInfo.getSymbol(), new BarMerger(symbolInfo, timeframe, strategyTO.getCheckRulesEveryTime(), strategyTO.getCheckRulesEveryTimeValue(), this::barDoneCallback));
                        if (symbolInfo.getSymbol().equals(candlestickEvent.getSymbol())) {
                            handleCandlestickCallback(symbolInfo, candlestickEvent);
                        }
                    }
                });
    }

    @Override
    public void barDoneCallback(SymbolInfo symbolInfo, Bar bar) {
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

    @Override
    public void shouldEnterExit(SymbolInfo symbolInfo, Bar bar) {
        BarSeries bs = barSeries.get(symbolInfo.getSymbol());
        final int endIndex = bs.getEndIndex();
        final Trade openTrade = TradeStrategyServiceUtil.getOpenTrade(symbolInfo, trades, strategyTO.getName());
        TradingRecord tradingRecord = tradingRecordMap.get(symbolInfo.getSymbol());
        log.debug("{} symbolInfo={} price={} openTrade={} price={}", strategyTO.getName(), symbolInfo.getSymbol(), bar.getClosePrice(), openTrade != null, openTrade != null ? openTrade.getExpectedPriceOpen() : 0);
        if (openTrade == null && strategies.get(symbolInfo.getSymbol()).shouldEnter(endIndex)) {
            openOrder(symbolInfo, bar);
            boolean enter = tradingRecord.enter(endIndex, bar.getClosePrice(), DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo)));
            log.info("Order enter = {}", enter);
        } else if (openTrade != null) {
            if (strategies.get(symbolInfo.getSymbol()).shouldExit(endIndex, tradingRecord)) {
                closeOrder(symbolInfo, bar, openTrade);
                boolean exit = tradingRecord.exit(endIndex, bar.getClosePrice(), DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo)));
                log.info("Order exit = {}", exit);
            } else if (user != null) {
                List<StrategyStatusInfoTO> strategyStatusInfoTOList = getStrategyStatusInfoToList();
                String message = TradeStrategyServiceUtil.getStrategyStatusInfoMessage(strategyStatusInfoTOList);
                telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), message);
            }
        }
    }

    @Override
    public void openOrder(SymbolInfo symbolInfo, Bar bar) {
        log.info("{} openOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        Trade trade = new Trade(strategyTO.getSeqId(), strategyTO.getUserId(), Exchange.BINANCE, TradeDirection.LONG.name(), symbolInfo.getSymbol(), symbolInfo.getOrderSize());
        tradeService.addTradeToOpenTradeList(trades, trade, symbolInfo.getOrderSize());
        orderByBitService.openTrade(byBitProxy, symbolInfoUtil, trade, bar, symbolInfo.getOrderSize(), user, debug);
    }

    @Override
    public void closeOrder(SymbolInfo symbolInfo, Bar bar, Trade openTrade) {
        log.info("{} closeOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        orderByBitService.closeTrade(byBitProxy, symbolInfoUtil, openTrade, bar, symbolInfo.getOrderSize(), user, debug);
        tradeService.removeTradeToOpenTradeList(trades, openTrade);
    }

    @Override
    public void closeOrderManually(SymbolInfo symbolInfo, Trade openTrade) {
        Bar bar = barSeries.get(symbolInfo.getSymbol()).getLastBar();
        closeOrder(symbolInfo, bar, openTrade);
    }

    @Override
    public void handleCandlestickCallback(SymbolInfo symbolInfo, CandlestickEvent candlestickEvent) {
        MDC.put("StrategyName", strategyTO.getName());
        log.trace("{} currencyPair={} price={}", strategyTO.getName(), symbolInfo.getSymbol(), candlestickEvent.getClose());
        candlestickEventLastTimes.put(symbolInfo.getSymbol(), LocalDateTime.now());
        candleMerger.get(symbolInfo.getSymbol()).addNewPrice(
                candlestickEvent.getEventTime(),
                new BigDecimal(candlestickEvent.getOpen()),
                new BigDecimal(candlestickEvent.getClose()),
                new BigDecimal(candlestickEvent.getVolume()),
                candlestickEvent.getIsBarFinal()
        );
    }

    @Override
    public Map<String, LocalDateTime> getCandlestickEventLastTimes() {
        return candlestickEventLastTimes;
    }

    @Override
    public StrategyTO getStrategyTO() {
        return strategyTO;
    }

    @Override
    public Timeframe getTimeframe() {
        return timeframe;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public List<StrategyStatusInfoTO> getStrategyStatusInfoToList() {
        // return statusInfoService.getStrategyStatusInfoToList(strategyTO, barSeries, byBitMarketProxy);
        return null;
    }
}
