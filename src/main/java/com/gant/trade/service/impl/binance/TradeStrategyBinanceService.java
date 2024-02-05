package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiClientFactory;
import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.BinanceApiWebSocketClient;
import com.gant.binance.api.client.domain.market.CandlestickInterval;
import com.gant.trade.domain.Trade;
import com.gant.trade.model.mapper.CandlestickEventMapper;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.StrategyStatusInfoTO;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.impl.TradeStrategyServiceImpl;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.impl.binance.BinanceSymbolInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DecimalNum;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class TradeStrategyBinanceService extends TradeStrategyServiceImpl {

    private final OrderBinanceService orderBinanceService;
    private final HistoricalCandlesBinanceService historicalCandlesBinanceService;
    private final BinanceSymbolInfoUtil symbolInfoUtil;
    private final BinanceStatusInfoService binanceStatusInfoService;
    private BinanceApiRestClient binanceApiRestClient;
    private BinanceApiWebSocketClient binanceApiWebSocketClient;
    private List<Closeable> candlestickSubscriptions;
    private final CandlestickEventMapper candlestickEventMapper;

    public TradeStrategyBinanceService(ApplicationContext applicationContext, Boolean debug) {
        super(applicationContext, debug);
        this.orderBinanceService = applicationContext.getBean(OrderBinanceService.class);
        this.historicalCandlesBinanceService = applicationContext.getBean(HistoricalCandlesBinanceService.class);
        this.symbolInfoUtil = new BinanceSymbolInfoUtil(applicationContext);
        this.binanceStatusInfoService = applicationContext.getBean(BinanceStatusInfoService.class);
        this.candlestickEventMapper = applicationContext.getBean(CandlestickEventMapper.class);
    }

    @Override
    public boolean unsubscribe() {
        AtomicBoolean closed = new AtomicBoolean(false);
        candlestickSubscriptions.forEach(closeable -> {
            try {
                closeable.close();
                closed.set(true);
            } catch (IOException e) {
                log.error("candlestickSubscriptions", e);
            }
        });
        Map<String, LocalDateTime> candlestickEventLastTimes = null;
        return closed.get();
    }

    @Override
    public void exchangeConfiguration() throws Exception {
        if (binanceApiRestClient == null && binanceApiWebSocketClient == null) {
            ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(getUser(), Exchange.BINANCE);
            if (userExchange != null) {
                BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance(userExchange.getApiKey(), userExchange.getSecretKey());
                binanceApiRestClient = binanceApiClientFactory.newRestClient();
                binanceApiWebSocketClient = binanceApiClientFactory.newWebSocketClient();
            }
        }
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        return getStrategyTO().getSymbolInfo().stream().map(currency ->
                        symbolInfoUtil.getSymbolInfoByExchange(binanceApiRestClient, getStrategyTO().getExchange(), getStrategyTO().getUserId(), currency.getSymbol(), currency.getOrderSize()))
                .collect(Collectors.toList());
    }

    @Override
    public void requestHistoricalCandles() throws Exception {
        historicalCandlesBinanceService.requestHistoricalCandles(binanceApiRestClient, getTimeframe(), getCurrencies()).forEach((k, v) -> getBarSeries().put(k.getSymbolInfo(), v));
    }

    @Override
    public void registerCandles() {
        log.info("{} Register candles", getStrategyTO().getName());
        candlestickSubscriptions = getCurrencies().stream().map(currency -> {
                    getCandleMerger().put(currency.getSymbol(), new BarMerger(currency, getTimeframe(), getStrategyTO().getCheckRulesEveryTime(), getStrategyTO().getCheckRulesEveryTimeValue(), this::barDoneCallback));

                    return binanceApiWebSocketClient.onCandlestickEvent(
                            currency.getSymbol().toLowerCase(),
                            CandlestickInterval.valueOf(getTimeframe().name()),
                            candlestickEvent -> handleCandlestickCallback(currency, candlestickEventMapper.map(candlestickEvent))
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public DecimalNum getPrecision(SymbolInfoTO symbolInfo) {
        return DecimalNum.valueOf(symbolInfoUtil.getPrecision(symbolInfo));
    }

    @Override
    public void openOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade trade) {
        orderBinanceService.openTrade(binanceApiRestClient, symbolInfoUtil, trade, bar, symbolInfo.getOrderSize(), getUser(), isDebug());
    }

    @Override
    public void closeOrderExchange(SymbolInfoTO symbolInfo, Bar bar, Trade openTrade) {
        orderBinanceService.closeTrade(binanceApiRestClient, symbolInfoUtil, openTrade, bar, symbolInfo.getOrderSize(), getUser(), isDebug());
    }

    @Override
    public List<StrategyStatusInfoTO> getStrategyStatusInfoToList() {
        return binanceStatusInfoService.getStrategyStatusInfoToList(getStrategyTO(), getBarSeries(), binanceApiRestClient);
    }
}
