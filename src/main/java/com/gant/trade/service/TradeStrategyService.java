package com.gant.trade.service;

import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.model.CandlestickEvent;
import com.gant.trade.model.Timeframe;
import com.gant.trade.rest.model.StrategyStatusInfoTO;
import com.gant.trade.rest.model.StrategyTO;
import com.gant.trade.rest.model.SymbolInfoTO;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TradeStrategyService {

    boolean status();

    boolean stop();

    void start(StrategyTO strategyTO, User user);

    void registerCandles() throws IOException;

    void barDoneCallback(SymbolInfoTO symbolInfo, Bar bar);

    void shouldEnterExit(SymbolInfoTO symbolInfo, Bar bar);

    void openOrder(SymbolInfoTO symbolInfo, Bar bar);

    void closeOrder(SymbolInfoTO symbolInfo, Bar bar, Trade openTrade);

    void closeOrderManually(SymbolInfoTO symbolInfo, Trade openTrade);

    void handleCandlestickCallback(SymbolInfoTO symbolInfo, CandlestickEvent candlestickEvent);

    Map<String, LocalDateTime> getCandlestickEventLastTimes();

    StrategyTO getStrategyTO();

    Timeframe getTimeframe();

    boolean isDebug();

    List<StrategyStatusInfoTO> getStrategyStatusInfoToList();
}
