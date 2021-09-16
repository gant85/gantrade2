package com.gant.trade.service;

import com.gant.trade.domain.Trade;
import org.ta4j.core.Bar;

public interface OrderService<T> {

    void openTrade(T exchange, Trade trade, Bar bar, double orderSize, boolean debug);

    void closeTrade(T exchange, Trade trade, Bar bar,double orderSize, boolean debug);

}
