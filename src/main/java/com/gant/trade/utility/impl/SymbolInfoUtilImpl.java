package com.gant.trade.utility.impl;

import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.mongo.repository.TradeRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.StrategyTO;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.utility.DecimalFormatUtil;
import com.gant.trade.utility.SymbolInfoUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

public class SymbolInfoUtilImpl<T> implements SymbolInfoUtil {

    private final UserRepository userRepository;
    private final StrategyService strategyService;
    private final TradeRepository tradeRepository;

    public SymbolInfoUtilImpl(ApplicationContext applicationContext) {
        this.userRepository = applicationContext.getBean(UserRepository.class);
        this.strategyService = applicationContext.getBean(StrategyService.class);
        this.tradeRepository = applicationContext.getBean(TradeRepository.class);
    }

    public SymbolInfoTO getSymbolInfoByExchange(T exchangeClient, Exchange exchange, long userId, String symbol, String orderSize) {
        User user = userRepository.findBySeqId(userId);
        if (user != null) {
            ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, exchange);
            if (userExchange != null) {
                return convert(exchangeClient, symbol, orderSize);
            }
        }
        return null;
    }

    public SymbolInfoTO getSymbolInfoByTrade(Trade trade) {
        StrategyTO strategy = strategyService.getStrategyById(trade.getStrategyId());
        return strategy.getSymbolInfo().stream().filter(symbol -> symbol.getSymbol().equals(trade.getSymbol())).findFirst().orElse(null);
    }

    public double getAmount(double price, SymbolInfoTO symbolInfo) {
        double amount = Double.parseDouble(symbolInfo.getOrderSize()) / price;
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public double getAmountPrecision(double amount, SymbolInfoTO symbolInfo) {
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public int getPrecision(SymbolInfoTO symbolInfo) {
        return (int) DecimalFormatUtil.round(-Math.log10(Double.parseDouble(symbolInfo.getStepSize())), symbolInfo.getBaseAssetPrecision());
    }

    public SymbolInfoTO convert(T exchangeClient, String symbol, String orderSize) {
        return null;
    }
}
