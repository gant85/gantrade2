package com.gant.trade.proxy.bybit.v5;

import com.gant.trade.proxy.bytbit.v5.MarketApi;
import com.gant.trade.proxy.bytbit.v5.model.GetKlineResponse;
import com.gant.trade.proxy.bytbit.v5.model.InstrumentsInfoResponse;
import com.gant.trade.proxy.bytbit.v5.model.TickersResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class ByBitMarketProxy {

    @Autowired
    private MarketApi marketApi;

    public GetKlineResponse getKline(String symbol, String interval, String category, Long start, Long end, Long limit) {
        return marketApi.getKline(symbol, interval, category, start, end, limit);
    }

    public InstrumentsInfoResponse instrumentsInfo(String category, String symbol, String status, String baseCoin, Long limit, String cursor) throws RestClientException {
        return marketApi.instrumentsInfo(category, symbol, status, baseCoin, limit, cursor);
    }

    public TickersResponse tickers(String category, String symbol, String expDate, String baseCoin) throws RestClientException {
        return marketApi.tickers(category, symbol, expDate, baseCoin);
    }
}
