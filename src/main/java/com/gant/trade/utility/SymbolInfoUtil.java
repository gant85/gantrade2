package com.gant.trade.utility;

import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;

public interface SymbolInfoUtil {

    SymbolInfo getSymbolInfoByTrade(Trade trade);

    double getAmount(double price, SymbolInfo symbolInfo);

    double getAmountPrecision(double amount, SymbolInfo symbolInfo);

    int getPrecision(SymbolInfo symbolInfo);
}
