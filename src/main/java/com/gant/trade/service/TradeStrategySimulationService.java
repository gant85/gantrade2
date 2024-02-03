package com.gant.trade.service;

import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.Candlestick;
import com.gant.trade.rest.model.StrategySimulationRequest;
import com.gant.trade.rest.model.StrategySimulationResponse;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.List;

public interface TradeStrategySimulationService {

    StrategySimulationResponse simulation(StrategySimulationRequest strategySimulationRequest);

    List<Candlestick> getCandlesticks(SymbolInfo symbolInfo, LocalDate startDate, LocalDate endDate);

    void barDoneCallback(final SymbolInfo symbolInfo, final Bar bar);

    void shouldEnterExit(final SymbolInfo symbolInfo, final Bar bar);

    void buildStrategySimulation(Bar bar);
}
