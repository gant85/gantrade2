package com.gant.trade.proxy.bybit.v5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gant.trade.proxy.bytbit.v5.OrderApi;
import com.gant.trade.proxy.bytbit.v5.model.OrderCreateRequest;
import com.gant.trade.proxy.bytbit.v5.model.OrderCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestClientException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

@Slf4j
public class ByBitOrderProxy {

    private final BybitEncryption bybitEncryption;
    private final OrderApi byBitOrderApi;

    public ByBitOrderProxy(BybitEncryption bybitEncryption, ApplicationContext applicationContext) {
        this.bybitEncryption = bybitEncryption;
        this.byBitOrderApi = (OrderApi) applicationContext.getBean("byBitOrderApi");
    }

    public OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest) throws RestClientException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        String signature = bybitEncryption.genPostSign(orderCreateRequest, timestamp);
        return byBitOrderApi.createOrder(bybitEncryption.getApiKey(), signature, bybitEncryption.getSignType(), timestamp, bybitEncryption.getRecvWindow(), orderCreateRequest);
    }
}
