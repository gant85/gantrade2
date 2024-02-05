package com.gant.trade.service.impl.bybit;

import com.gant.trade.domain.Trade;
import com.gant.trade.proxy.bybit.v5.ByBitProxy;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.StrategyStatusInfoTO;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.impl.TradeStrategyServiceImpl;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.impl.bybit.ByBitSymbolInfoUtil;
import com.gant.trade.websocket.bybit.ByBitWebSocketClient;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TradeStrategyByBitService extends TradeStrategyServiceImpl {

    private final OrderByBitService orderByBitService;
    private ByBitProxy byBitProxy;
    private ByBitWebSocketClient byBitWebSocketClient;
    private final HistoricalCandlesByBitService historicalCandlesByBitService;
    private final ByBitSymbolInfoUtil symbolInfoUtil;
    private final ByBitStatusInfoService statusInfoService;
    private Disposable klinesSubscriber;

    public TradeStrategyByBitService(ApplicationContext applicationContext, Boolean debug) {
        super(applicationContext, debug);
        this.orderByBitService = applicationContext.getBean(OrderByBitService.class);
        this.historicalCandlesByBitService = applicationContext.getBean(HistoricalCandlesByBitService.class);
        this.symbolInfoUtil = new ByBitSymbolInfoUtil(applicationContext);
        this.statusInfoService = applicationContext.getBean(ByBitStatusInfoService.class);
    }

    @Override
    public boolean unsubscribe() {
        try {
            klinesSubscriber.dispose();
            return true;
        } catch (Exception e) {
            log.error("klinesSubscriber", e);
            return false;
        }
    }

    @Override
    public void exchangeConfiguration() throws Exception {
        ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(getUser(), Exchange.BYBIT);
        if (userExchange != null) {
            this.byBitProxy = new ByBitProxy(getApplicationContext(), userExchange);
            this.byBitWebSocketClient = new ByBitWebSocketClient(getBotConfig().getExchange().getBybit().getWebSocket().getBasePath(), byBitProxy.getBybitEncryption());
            this.byBitWebSocketClient.connect();
        }
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        return getStrategyTO().getSymbolInfo().stream().map(currency ->
                        symbolInfoUtil.getSymbolInfoByExchange(byBitProxy, getStrategyTO().getExchange(), getStrategyTO().getUserId(), currency.getSymbol(), currency.getOrderSize()))
                .collect(Collectors.toList());
    }

    @Override
    public void requestHistoricalCandles() throws Exception {
        Long startTime = LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        historicalCandlesByBitService.requestHistoricalCandles(byBitProxy, getTimeframe(), getCurrencies(), startTime, endTime).forEach((k, v) -> getBarSeries().put(k.getSymbolInfo(), v));
    }

    @Override
    public void registerCandles() throws IOException {
        log.info("{} Register candles", getStrategyTO().getName());
        klinesSubscriber = byBitWebSocketClient.subscribeKlines(getCurrencies(), getTimeframe())
                .subscribe(candlestickEvent -> {
                    for (SymbolInfoTO symbolInfo : getCurrencies()) {
                        getCandleMerger().put(symbolInfo.getSymbol(), new BarMerger(symbolInfo, getTimeframe(), getStrategyTO().getCheckRulesEveryTime(), getStrategyTO().getCheckRulesEveryTimeValue(), this::barDoneCallback));
                        if (symbolInfo.getSymbol().equals(candlestickEvent.getSymbol())) {
                            handleCandlestickCallback(symbolInfo, candlestickEvent);
                        }
                    }
                });
    }

    @Override
    public DecimalNum getPrecision(SymbolInfoTO symbolInfo) {
        return DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo));
    }

    @Override
    public void openOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade trade) {
        orderByBitService.openTrade(byBitProxy, symbolInfoUtil, trade, bar, symbolInfo.getOrderSize(), getUser(), isDebug());
    }

    @Override
    public void closeOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade openTrade) {
        orderByBitService.closeTrade(byBitProxy, symbolInfoUtil, openTrade, bar, symbolInfo.getOrderSize(), getUser(), isDebug());
    }

    @Override
    public List<StrategyStatusInfoTO> getStrategyStatusInfoToList() {
        return statusInfoService.getStrategyStatusInfoToList(getStrategyTO(), getBarSeries(), byBitProxy);
    }
}
