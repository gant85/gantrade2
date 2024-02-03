package com.gant.trade.utility.impl;

import com.gant.trade.domain.Strategy;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.mongo.repository.StrategyRepository;
import com.gant.trade.mongo.repository.TradeRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.utility.DecimalFormatUtil;
import com.gant.trade.utility.SymbolInfoUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

public class SymbolInfoUtilImpl<T> implements SymbolInfoUtil {

    private final UserRepository userRepository;
    private final StrategyRepository strategyRepository;
    private final TradeRepository tradeRepository;

    public SymbolInfoUtilImpl(ApplicationContext applicationContext) {
        this.userRepository = applicationContext.getBean(UserRepository.class);
        this.strategyRepository = applicationContext.getBean(StrategyRepository.class);
        this.tradeRepository = applicationContext.getBean(TradeRepository.class);
    }

    public SymbolInfo getSymbolInfoByExchange(T exchangeClient, Exchange exchange, long userId, String symbol, double orderSize) {
        User user = userRepository.findBySeqId(userId);
        if (user != null) {
            ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, exchange);
            if (userExchange != null) {
                return convert(exchangeClient, symbol, orderSize);
            }
        }
        return null;
    }

    public SymbolInfo getSymbolInfoByTrade(Trade trade) {
        Strategy strategy = strategyRepository.findBySeqId(trade.getStrategyId());
        return strategy.getSymbolInfo().stream().filter(symbol -> symbol.getSymbol().equals(trade.getSymbol())).findFirst().orElse(null);
    }

    public double getAmount(double price, SymbolInfo symbolInfo) {
        double amount = symbolInfo.getOrderSize() / price;
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public double getAmountPrecision(double amount, SymbolInfo symbolInfo) {
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public int getPrecision(SymbolInfo symbolInfo) {
        return (int) DecimalFormatUtil.round(-Math.log10(Double.parseDouble(symbolInfo.getStepSize())), symbolInfo.getBaseAssetPrecision());
    }

    public SymbolInfo convert(T exchangeClient, String symbol, double orderSize) {
        return null;
    }
}
