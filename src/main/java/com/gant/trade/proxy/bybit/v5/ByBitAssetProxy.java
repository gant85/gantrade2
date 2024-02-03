package com.gant.trade.proxy.bybit.v5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gant.trade.proxy.bytbit.v5.AssetApi;
import com.gant.trade.proxy.bytbit.v5.model.InterTransferRequest;
import com.gant.trade.proxy.bytbit.v5.model.InterTransferResponse;
import com.gant.trade.proxy.bytbit.v5.model.QueryRecordResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestClientException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

@Slf4j
public class ByBitAssetProxy {

    private final BybitEncryption bybitEncryption;
    private final AssetApi byBitAssetApi;

    public ByBitAssetProxy(BybitEncryption bybitEncryption, ApplicationContext applicationContext) {
        this.bybitEncryption = bybitEncryption;
        this.byBitAssetApi = (AssetApi) applicationContext.getBean("byBitAssetApi");
    }


    public QueryRecordResponse queryRecord(Long startTime, Long endTime, String coin, String cursor, Long limit) throws RestClientException, NoSuchAlgorithmException, InvalidKeyException {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        if (startTime != null) {
            params.put("startTime", startTime);
        }
        if (endTime != null) {
            params.put("endTime", endTime);
        }
        if (coin != null) {
            params.put("symbol", coin);
        }
        if (cursor != null) {
            params.put("cursor", cursor);
        }
        if (limit != null) {
            params.put("startTime", limit);
        }
        String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        String signature = bybitEncryption.genGetSign(params, timestamp);

        return byBitAssetApi.queryRecord(bybitEncryption.getApiKey(), signature, bybitEncryption.getSignType(), timestamp, bybitEncryption.getRecvWindow(), startTime, endTime, coin, cursor, limit);
    }

    public InterTransferResponse interTransfer(InterTransferRequest interTransferRequest) throws RestClientException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        String signature = bybitEncryption.genPostSign(interTransferRequest, timestamp);

        return byBitAssetApi.interTransfer(bybitEncryption.getApiKey(), signature, bybitEncryption.getSignType(), timestamp, bybitEncryption.getRecvWindow(), interTransferRequest);
    }
}
