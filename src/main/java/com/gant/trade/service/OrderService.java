package com.gant.trade.service;

import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import org.ta4j.core.Bar;

public interface OrderService<T, Z> {

    void openTrade(T exchange, Z symbolInfoUtil, Trade trade, Bar bar, String orderSize, User user, boolean debug);

    void closeTrade(T exchange, Z symbolInfoUtil, Trade trade, Bar bar, String orderSize, User user, boolean debug);

}
