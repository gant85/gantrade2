package com.gant.trade.service.impl;

import com.gant.trade.config.BotConfig;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.model.CandlestickEvent;
import com.gant.trade.model.Timeframe;
import com.gant.trade.model.TradeDirection;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.mongo.service.UserService;
import com.gant.trade.rest.model.*;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.service.TradeStrategyService;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.BarSeriesUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import lombok.Getter;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class TradeStrategyServiceImpl implements TradeStrategyService {

    private final ApplicationContext applicationContext;
    private final BotConfig botConfig;
    private final boolean debug;
    private StrategyTO strategyTO;
    private Timeframe timeframe;
    private Map<String, BarMerger> candleMerger;
    private Map<String, LocalDateTime> candlestickEventLastTimes;
    private List<SymbolInfoTO> currencies;
    private Map<String, BarSeries> barSeries;
    private final TradeService tradeService;
    private Map<String, List<Trade>> trades;
    private Map<String, TradingRecord> tradingRecordMap;
    private Map<String, Strategy> strategies;
    private User user;
    private int maxRetry = 0;
    private final TelegramBotService telegramBotService;
    private final UserService userService;

    public TradeStrategyServiceImpl(ApplicationContext applicationContext, Boolean debug) {
        this.applicationContext = applicationContext;
        this.botConfig = applicationContext.getBean(BotConfig.class);
        this.debug = debug;
        this.tradeService = applicationContext.getBean(TradeService.class);
        this.telegramBotService = applicationContext.getBean(TelegramBotService.class);
        this.userService = applicationContext.getBean(UserService.class);
    }

    @Override
    public boolean status() {
        return candlestickEventLastTimes.values().stream()
                .allMatch(candlestickEventLastTime -> Duration.between(candlestickEventLastTime, LocalDateTime.now()).toMinutes() < botConfig.getHealthCheck());
    }

    @Override
    public boolean stop() {
        AtomicBoolean stopped = new AtomicBoolean(false);
        try {
            unsubscribe();
            stopped.set(true);
        } catch (Exception e) {
            stopped.set(false);
            log.error("candlestickSubscriptions", e);
        }
        return stopped.get();
    }

    @Override
    public boolean unsubscribe() {
        // Override this in the exchange implementation
        return false;
    }

    @Override
    public void start(StrategyTO strategyTO, User user) {
        try {
            MDC.put("StrategyName", strategyTO.getName());
            this.strategyTO = strategyTO;
            this.user = user;
            this.timeframe = Timeframe.getTimeframe(strategyTO.getTimeframe().toString());
            this.candleMerger = new HashMap<>();
            this.candlestickEventLastTimes = new HashMap<>();
            this.barSeries = new HashMap<>();
            exchangeConfiguration();
            this.currencies = getCurrencies();
            requestHistoricalCandles();
            this.trades = this.tradeService.getOpenTradesByStrategy(strategyTO.getSeqId());
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

    @Override
    public void exchangeConfiguration() throws Exception {
        // Override this in the exchange implementation
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        // Override this in the exchange implementation
        return null;
    }

    @Override
    public void requestHistoricalCandles() throws Exception {
        // Override this in the exchange implementation
    }

    @Override
    public void registerCandles() throws IOException {
        // Override this in the exchange implementation
    }

    @Override
    public void barDoneCallback(SymbolInfoTO symbolInfo, Bar bar) {
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
    public void shouldEnterExit(SymbolInfoTO symbolInfo, Bar bar) {
        BarSeries bs = barSeries.get(symbolInfo.getSymbol());
        final int endIndex = bs.getEndIndex();
        final Trade openTrade = TradeStrategyServiceUtil.getOpenTrade(symbolInfo, trades, strategyTO.getName());
        TradingRecord tradingRecord = tradingRecordMap.get(symbolInfo.getSymbol());
        log.debug("{} symbolInfo={} price={} openTrade={} price={}", strategyTO.getName(), symbolInfo.getSymbol(), bar.getClosePrice(), openTrade != null, openTrade != null ? openTrade.getExpectedPriceOpen() : 0);
        if (openTrade == null && strategies.get(symbolInfo.getSymbol()).shouldEnter(endIndex)) {
            openOrder(symbolInfo, bar);
            boolean enter = tradingRecord.enter(endIndex, bar.getClosePrice(), getPrecision(symbolInfo));
            log.info("Order enter = {}", enter);
        } else if (openTrade != null) {
            if (strategies.get(symbolInfo.getSymbol()).shouldExit(endIndex, tradingRecord)) {
                closeOrder(symbolInfo, bar, openTrade);
                boolean exit = tradingRecord.exit(endIndex, bar.getClosePrice(), getPrecision(symbolInfo));
                log.info("Order exit = {}", exit);
            } else if (user != null) {
                List<StrategyStatusInfoTO> strategyStatusInfoTOList = getStrategyStatusInfoToList();
                String message = TradeStrategyServiceUtil.getStrategyStatusInfoMessage(strategyStatusInfoTOList);
                telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), message);
            }
        }
    }

    public DecimalNum getPrecision(SymbolInfoTO symbolInfo) {
        // Override this in the exchange implementation
        return null;
    }

    @Override
    public void openOrder(SymbolInfoTO symbolInfo, Bar bar) {
        log.info("{} openOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        Trade trade = new Trade(strategyTO.getSeqId(), strategyTO.getUserId(), Exchange.BINANCE, TradeDirection.LONG.name(), symbolInfo.getSymbol(), Double.parseDouble(symbolInfo.getOrderSize()));
        tradeService.addTradeToOpenTradeList(trades, trade);
        openOrderExchange(symbolInfo, bar, trade);
    }

    public void openOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade trade) {
        // Override this in the exchange implementation
    }

    @Override
    public void closeOrder(SymbolInfoTO symbolInfo, Bar bar, Trade openTrade) {
        log.info("{} closeOrder: symbolInfo={} lastClosePrice={}", strategyTO.getName(), symbolInfo, bar.getClosePrice());
        closeOrderExchange(symbolInfo, bar, openTrade);
        tradeService.removeTradeToOpenTradeList(trades, openTrade);
    }

    public void closeOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade openTrade) {
        // Override this in the exchange implementation
    }

    @Override
    public void closeOrderManually(SymbolInfoTO symbolInfo, Trade openTrade) {
        Bar bar = barSeries.get(symbolInfo.getSymbol()).getLastBar();
        closeOrder(symbolInfo, bar, openTrade);
    }

    @Override
    public void handleCandlestickCallback(SymbolInfoTO symbolInfo, CandlestickEvent candlestickEvent) {
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
        // Override this in the exchange implementation
        return null;
    }
}
