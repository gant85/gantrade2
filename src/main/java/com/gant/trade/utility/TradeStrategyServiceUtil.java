package com.gant.trade.utility;


import com.gant.trade.domain.Order;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.strategy.TradeStrategy;
import com.gant.trade.rest.model.StrategyTO;
import com.gant.trade.rest.model.TradeState;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TradeStrategyServiceUtil {

    private TradeStrategyServiceUtil(){}

    public static Map<String, Strategy> getStrategies(List<SymbolInfo> currencies, Map<String, BarSeries> barSeries, StrategyTO strategyTO) {
        return currencies.stream().collect(Collectors.toMap(SymbolInfo::getSymbol, symbolInfo1 -> new TradeStrategy(barSeries.get(symbolInfo1.getSymbol()), strategyTO).getStrategy()));
    }

    public static Map<String, TradingRecord> getTradingRecordMap(List<SymbolInfo> symbolInfos, Map<String, List<Trade>> tradeMap, Map<String, BarSeries> barSeries, String strategyName) {
        Map<String, TradingRecord> tradingRecordMap = new HashMap<>();
        for (SymbolInfo symbolInfo : symbolInfos) {
            List<Trade> trades = tradeMap.get(symbolInfo.getSymbol());
            if (trades == null) {
                trades = new ArrayList<>();
            }
            if (trades.size() > 1) {
                throw new IllegalArgumentException(strategyName + " More then one open trade for " + symbolInfo + " / " + trades);
            }
            TradingRecord tradingRecord = new BaseTradingRecord();
            if (!trades.isEmpty()) {
                Trade trade = trades.get(0);
                Order buyOrder = trade.getOrders().get(0);
                if (buyOrder != null) {
                    Num buyPrice = DecimalNum.valueOf(buyOrder.getPrice());
                    BarSeries bs = barSeries.get(symbolInfo.getSymbol());
                    boolean enter = false;
                    for (int i = 0; i < bs.getBarData().size(); i++) {
                        Bar bar = bs.getBar(i);
                        if (bar.getClosePrice().doubleValue() > buyPrice.doubleValue() && bar.getClosePrice().doubleValue() < (buyPrice.doubleValue() * 1.001) && !enter) {
                            log.debug("buyOrder buyPrice={} barIndex={} barClosePrice={}", buyPrice.doubleValue(), i, bar.getClosePrice().doubleValue());
                            enter = tradingRecord.enter(i, buyPrice, DecimalNum.valueOf(symbolInfo.getBaseAssetPrecision()));
                        }
                    }
                    if (!enter) {
                        tradingRecord.enter(0, buyPrice, DecimalNum.valueOf(symbolInfo.getBaseAssetPrecision()));
                    }
                }
            }
            tradingRecordMap.put(symbolInfo.getSymbol(), tradingRecord);
        }
        return tradingRecordMap;
    }

    public static Trade getOpenTrade(SymbolInfo symbolInfo, Map<String, List<Trade>> tradeMap, String strategyName) {
        final List<Trade> tradeList = tradeMap.get(symbolInfo.getSymbol());
        if (tradeList == null) {
            return null;
        }
        final List<Trade> openTrades = tradeList.stream().filter(t -> TradeState.OPEN.equals(t.getTradeState())).collect(Collectors.toList());
        if (openTrades.size() > 1) {
            throw new IllegalArgumentException(strategyName + " More then one open trade for " + symbolInfo + " / " + openTrades);
        }
        if (openTrades.isEmpty()) {
            return null;
        }
        return openTrades.get(0);
    }

    public static ExchangeConfiguration getExchangeConfigurationByExchange(User user,Exchange exchange){
        return user.getExchangeConfiguration().stream()
                .filter(exchangeConfiguration -> exchangeConfiguration.getExchangeTO().getExchange() == exchange).findFirst().orElse(null);
    }
}
