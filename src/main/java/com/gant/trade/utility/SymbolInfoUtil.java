package com.gant.trade.utility;


import com.gant.trade.domain.Trade;
import com.gant.trade.rest.model.SymbolInfoTO;

public interface SymbolInfoUtil {

    SymbolInfoTO getSymbolInfoByTrade(Trade trade);

    double getAmount(double price, SymbolInfoTO symbolInfo);

    double getAmountPrecision(double amount, SymbolInfoTO symbolInfo);

    int getPrecision(SymbolInfoTO symbolInfo);
}
