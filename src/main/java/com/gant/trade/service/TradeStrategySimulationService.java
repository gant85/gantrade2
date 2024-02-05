package com.gant.trade.service;

import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.Timeframe;
import com.gant.trade.rest.model.StrategySimulationRequest;
import com.gant.trade.rest.model.StrategySimulationResponse;
import com.gant.trade.rest.model.SymbolInfoTO;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.List;

public interface TradeStrategySimulationService {

    StrategySimulationResponse simulation(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException;

    void exchangeConfiguration(Long userId) throws BusinessRuntimeException;

    List<SymbolInfoTO> getCurrencies();

    void requestHistoricalCandles(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException;

    List<Candlestick> getCandlesticks(SymbolInfoTO symbolInfo, LocalDate startDate, LocalDate endDate) throws BusinessRuntimeException;

    List<Candlestick> getCandlesticksExchange(Timeframe timeframe, SymbolInfoTO symbolInfo, Long startTime, Long endTime) throws BusinessRuntimeException;

    void barDoneCallback(final SymbolInfoTO symbolInfo, final Bar bar);

    void shouldEnterExit(final SymbolInfoTO symbolInfo, final Bar bar);

    void buildStrategySimulation(Bar bar);
}
