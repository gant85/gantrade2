package com.gant.trade.service;

import com.gant.binance.api.client.domain.event.CandlestickEvent;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.Timeframe;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.rest.model.StrategyTO;
import org.ta4j.core.Bar;

import java.time.LocalDateTime;
import java.util.Map;

public interface TradeStrategyService {

    boolean status();

    boolean stop();

    void start(StrategyTO strategyTO, User user);

    void registerCandles();

    void barDoneCallback(SymbolInfo symbolInfo, Bar bar);

    void shouldEnterExit(SymbolInfo symbolInfo, Bar bar);

    void openOrder(SymbolInfo symbolInfo, Bar bar);

    void closeOrder(SymbolInfo symbolInfo, Bar bar, Trade openTrade);

    void closeOrderManually(SymbolInfo symbolInfo, Trade openTrade);

    void handleCandlestickCallback(SymbolInfo symbolInfo, CandlestickEvent candlestickEvent);

    Map<String, LocalDateTime> getCandlestickEventLastTimes();

    StrategyTO getStrategyTO();

    Timeframe getTimeframe();

    boolean isDebug();
}
