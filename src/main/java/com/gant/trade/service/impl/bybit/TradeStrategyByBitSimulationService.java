package com.gant.trade.service.impl.bybit;

import com.gant.trade.domain.User;
import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.Timeframe;
import com.gant.trade.proxy.bybit.v5.ByBitProxy;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.StrategySimulationRequest;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.impl.TradeStrategySimulationServiceImpl;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.impl.bybit.ByBitSymbolInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeStrategyByBitSimulationService extends TradeStrategySimulationServiceImpl {

    @Autowired
    private HistoricalCandlesByBitService historicalCandlesByBitService;
    private final ByBitSymbolInfoUtil symbolInfoUtil;
    private ByBitProxy byBitProxy;

    public TradeStrategyByBitSimulationService(ApplicationContext applicationContext) {
        super(applicationContext);
        this.symbolInfoUtil = new ByBitSymbolInfoUtil(applicationContext);
    }

    @Override
    public void exchangeConfiguration(Long userId) throws BusinessRuntimeException {
        User user = getUserRepository().findBySeqId(userId);
        ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, Exchange.BYBIT);
        if (userExchange != null) {
            this.byBitProxy = new ByBitProxy(getApplicationContext(), userExchange);
        }
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        return getStrategyTO().getSymbolInfo().stream().map(symbol ->
                        symbolInfoUtil.getSymbolInfoByExchange(byBitProxy, Exchange.BYBIT, getStrategyTO().getUserId(), symbol.getSymbol(), symbol.getOrderSize()))
                .collect(Collectors.toList());
    }

    @Override
    public void requestHistoricalCandles(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException {
        long startTime = strategySimulationRequest.getStartDate().atStartOfDay().minusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = strategySimulationRequest.getStartDate().atStartOfDay().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        historicalCandlesByBitService.requestHistoricalCandles(byBitProxy, getTimeframe(), getTradedCurrencies(), startTime, endTime)
                .forEach((k, v) -> getBarSeries().put(k.getSymbolInfo(), v));
    }

    @Override
    public List<Candlestick> getCandlesticksExchange(Timeframe timeframe, SymbolInfoTO symbolInfo, Long startTime, Long endTime) throws BusinessRuntimeException {
        return historicalCandlesByBitService.getCandlestickBars(byBitProxy, timeframe, symbolInfo, startTime, endTime);
    }

    @Override
    public Double getAmount(Double price, SymbolInfoTO symbolInfo) {
        return symbolInfoUtil.getAmount(price, symbolInfo);
    }
}
