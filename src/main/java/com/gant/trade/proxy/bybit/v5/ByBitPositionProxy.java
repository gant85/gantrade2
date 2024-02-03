package com.gant.trade.proxy.bybit.v5;

import com.gant.trade.proxy.bytbit.v5.PositionApi;
import com.gant.trade.proxy.bytbit.v5.model.ClosedPnlResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

@Slf4j
public class ByBitPositionProxy {

    private final BybitEncryption bybitEncryption;
    private final PositionApi byBitPositionApi;

    public ByBitPositionProxy(BybitEncryption bybitEncryption, ApplicationContext applicationContext) {
        this.bybitEncryption = bybitEncryption;
        this.byBitPositionApi = (PositionApi) applicationContext.getBean("byBitPositionApi");
    }


    public ClosedPnlResponse closedPnl(String category, Long startTime, Long endTime, String symbol, String cursor, Long limit) throws NoSuchAlgorithmException, InvalidKeyException {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("category", category);
        if (startTime != null) {
            params.put("startTime", startTime);
        }
        if (endTime != null) {
            params.put("endTime", endTime);
        }
        if (symbol != null) {
            params.put("symbol", symbol);
        }
        if (cursor != null) {
            params.put("cursor", cursor);
        }
        if (limit != null) {
            params.put("limit", limit);
        }
        String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        String signature = bybitEncryption.genGetSign(params, timestamp);

        return byBitPositionApi.closedPnl(bybitEncryption.getApiKey(), signature, bybitEncryption.getSignType(), timestamp, bybitEncryption.getRecvWindow(), category, startTime, endTime, symbol, cursor, limit);
    }
}
