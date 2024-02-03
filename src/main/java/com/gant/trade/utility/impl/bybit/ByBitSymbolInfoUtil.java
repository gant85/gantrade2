package com.gant.trade.utility.impl.bybit;

import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.proxy.bybit.v5.ByBitProxy;
import com.gant.trade.proxy.bytbit.v5.model.InstrumentsInfoResponse;
import com.gant.trade.proxy.bytbit.v5.model.InstrumentsInfoResponseRow;
import com.gant.trade.utility.impl.SymbolInfoUtilImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class ByBitSymbolInfoUtil extends SymbolInfoUtilImpl<ByBitProxy> {

    public ByBitSymbolInfoUtil(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public SymbolInfo convert(ByBitProxy exchangeClient, String symbol, double orderSize) {
        InstrumentsInfoResponse instrumentsInfoResponse = exchangeClient.getByBitMarketProxy().instrumentsInfo("linear", symbol, null, null, null, null);
        if (instrumentsInfoResponse == null || instrumentsInfoResponse.getResult() == null || instrumentsInfoResponse.getResult().getList() == null) {
            return null;
        }
        InstrumentsInfoResponseRow row = instrumentsInfoResponse.getResult().getList().get(0);
        SymbolInfo s = new SymbolInfo();
        s.setSymbol(row.getSymbol());
        s.setBaseAsset(row.getBaseCoin());
        s.setQuoteAsset(row.getQuoteCoin());
        if (row.getLotSizeFilter() != null) {
            s.setStepSize(row.getLotSizeFilter().getQtyStep());
            s.setMinQty(row.getLotSizeFilter().getMinOrderQty());
        }
        if (row.getPriceFilter() != null) {
            s.setTickSizePrice(row.getPriceFilter().getTickSize());
        }
        s.setOrderSize(orderSize);
        return s;
    }
}
