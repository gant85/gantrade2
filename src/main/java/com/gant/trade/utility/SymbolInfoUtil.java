package com.gant.trade.utility;

import com.gant.binance.api.client.BinanceApiClientFactory;
import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.domain.general.FilterType;
import com.gant.binance.api.client.domain.general.SymbolFilter;
import com.gant.trade.domain.Strategy;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.mongo.repository.StrategyRepository;
import com.gant.trade.mongo.repository.TradeRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.ExchangeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SymbolInfoUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StrategyRepository strategyRepository;

    @Autowired
    private TradeRepository tradeRepository;

    public SymbolInfo getSymbolInfoByExchange(Exchange exchange, long userId, String symbol,double orderSize){
        User user = userRepository.findBySeqId(userId);

        switch (exchange) {
            case BINANCE:
                //TODO verificare se è corretto creare una nuova instanza ogni volta
                if(user != null) {
                    ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user,Exchange.BINANCE);
                    if(userExchange!=null) {
                        BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance(userExchange.getApiKey(), userExchange.getSecretKey());
                        BinanceApiRestClient binanceApiRestClient = binanceApiClientFactory.newRestClient();
                        return convert(binanceApiRestClient.getExchangeInfo().getSymbolInfo(symbol),orderSize);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            case COINBASE:
                return null;
            default:
                return null;
        }
    }

    public SymbolInfo getSymbolInfoByTrade(Trade trade){
        Strategy strategy = strategyRepository.findBySeqId(trade.getStrategyId());
        return strategy.getSymbolInfo().stream().filter(symbol -> symbol.getSymbol().equals(trade.getSymbol())).findFirst().orElse(null);
    }

    public double getAmount(double price,SymbolInfo symbolInfo) {
        double amount = symbolInfo.getOrderSize() / price;
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public double getAmountPrecision(double amount,SymbolInfo symbolInfo){
        String quantity = DecimalFormatUtil.format(getPrecision(symbolInfo), amount, Locale.ENGLISH);
        return DecimalFormatUtil.round(Double.parseDouble(quantity), getPrecision(symbolInfo));
    }

    public int getPrecision(SymbolInfo symbolInfo) {
        return (int) DecimalFormatUtil.round(-Math.log10(Double.parseDouble(symbolInfo.getStepSize())), symbolInfo.getBaseAssetPrecision());
    }

    public static SymbolInfo convert(com.gant.binance.api.client.domain.general.SymbolInfo symbolInfo, double orderSize) {
        if (symbolInfo == null) {
            return null;
        }
        SymbolInfo s = new SymbolInfo();
        SymbolFilter symbolFilter = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
        s.setSymbol(symbolInfo.getSymbol());
        s.setBaseAsset(symbolInfo.getBaseAsset());
        s.setBaseAssetPrecision(symbolInfo.getBaseAssetPrecision());
        s.setQuoteAsset(symbolInfo.getQuoteAsset());
        s.setQuoteAssetPrecision(symbolInfo.getQuotePrecision());
        s.setStepSize(symbolFilter.getStepSize());
        s.setMinQty(symbolFilter.getMinQty());
        s.setTickSizePrice(symbolFilter.getTickSize());
        s.setOrderSize(orderSize);
        return s;
    }
}
