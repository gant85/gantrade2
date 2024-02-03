package com.gant.trade.utility.impl.binance;

import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.domain.general.FilterType;
import com.gant.binance.api.client.domain.general.SymbolFilter;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.utility.impl.SymbolInfoUtilImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class BinanceSymbolInfoUtil extends SymbolInfoUtilImpl<BinanceApiRestClient> {

    public BinanceSymbolInfoUtil(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public SymbolInfoTO convert(BinanceApiRestClient exchangeClient, String symbol, String orderSize) {
        com.gant.binance.api.client.domain.general.SymbolInfo symbolInfo = exchangeClient.getExchangeInfo().getSymbolInfo(symbol);
        if (symbolInfo == null) {
            return null;
        }

        SymbolInfoTO s = new SymbolInfoTO();
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
