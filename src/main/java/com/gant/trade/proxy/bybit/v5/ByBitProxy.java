package com.gant.trade.proxy.bybit.v5;

import com.gant.trade.rest.model.ExchangeConfiguration;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

@Getter
public class ByBitProxy {

    private BybitEncryption bybitEncryption;
    private ByBitAssetProxy byBitAssetProxy;
    private ByBitMarketProxy byBitMarketProxy;
    private ByBitOrderProxy byBitOrderProxy;
    private ByBitPositionProxy byBitPositionProxy;

    public ByBitProxy(ApplicationContext applicationContext, ExchangeConfiguration userExchange) {
        this.bybitEncryption = new BybitEncryption(userExchange.getApiKey(), userExchange.getSecretKey());
        this.byBitAssetProxy = new ByBitAssetProxy(bybitEncryption, applicationContext);
        this.byBitMarketProxy = applicationContext.getBean(ByBitMarketProxy.class);
        this.byBitOrderProxy = new ByBitOrderProxy(bybitEncryption, applicationContext);
        this.byBitPositionProxy = new ByBitPositionProxy(bybitEncryption, applicationContext);
    }
}
