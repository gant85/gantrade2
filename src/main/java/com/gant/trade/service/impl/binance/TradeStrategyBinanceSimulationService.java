package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiClientFactory;
import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.trade.domain.User;
import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.Timeframe;
import com.gant.trade.model.mapper.CandlestickMapper;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.StrategySimulationRequest;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.impl.TradeStrategySimulationServiceImpl;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.impl.binance.BinanceSymbolInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeStrategyBinanceSimulationService extends TradeStrategySimulationServiceImpl {

    @Autowired
    private HistoricalCandlesBinanceService historicalCandlesBinanceService;
    private final BinanceSymbolInfoUtil symbolInfoUtil;
    @Autowired
    private CandlestickMapper candlestickMapper;

    private BinanceApiRestClient binanceApiRestClient;

    public TradeStrategyBinanceSimulationService(ApplicationContext applicationContext) {
        super(applicationContext);
        this.symbolInfoUtil = new BinanceSymbolInfoUtil(applicationContext);
    }

    @Override
    public void exchangeConfiguration(Long userId) throws BusinessRuntimeException {
        if (this.binanceApiRestClient == null) {
            User user = getUserRepository().findBySeqId(userId);
            ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, Exchange.BINANCE);
            if (userExchange != null) {
                final BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance(userExchange.getApiKey(), userExchange.getSecretKey());
                this.binanceApiRestClient = binanceApiClientFactory.newRestClient();
            }
        }
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        return getStrategyTO().getSymbolInfo().stream().map(symbol ->
                        symbolInfoUtil.getSymbolInfoByExchange(binanceApiRestClient, Exchange.BINANCE, getStrategyTO().getUserId(), symbol.getSymbol(), symbol.getOrderSize()))
                .collect(Collectors.toList());
    }

    @Override
    public void requestHistoricalCandles(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException {
        long startTime = strategySimulationRequest.getStartDate().atStartOfDay().minusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = strategySimulationRequest.getStartDate().atStartOfDay().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        historicalCandlesBinanceService.requestHistoricalCandles(binanceApiRestClient, getTimeframe(), getTradedCurrencies(), startTime, endTime)
                .forEach((k, v) -> getBarSeries().put(k.getSymbolInfo(), v));
    }

    @Override
    public List<Candlestick> getCandlesticksExchange(Timeframe timeframe, SymbolInfoTO symbolInfo, Long startTime, Long endTime) {
        return historicalCandlesBinanceService.getCandlestickBars(binanceApiRestClient, timeframe, symbolInfo, startTime, endTime);
    }

    @Override
    public Double getAmount(Double price, SymbolInfoTO symbolInfo) {
        return symbolInfoUtil.getAmount(price, symbolInfo);
    }
}
