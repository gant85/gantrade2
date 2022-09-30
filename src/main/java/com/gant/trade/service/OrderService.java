package com.gant.trade.service;

import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import org.ta4j.core.Bar;

public interface OrderService<T> {

    void openTrade(T exchange, Trade trade, Bar bar, double orderSize, User user, boolean debug);

    void closeTrade(T exchange, Trade trade, Bar bar, double orderSize, User user, boolean debug);

}
