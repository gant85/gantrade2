package com.gant.trade.proxy.bybit.v5;

import com.gant.trade.proxy.bytbit.ApiClient;
import com.gant.trade.proxy.bytbit.v5.AssetApi;
import com.gant.trade.proxy.bytbit.v5.MarketApi;
import com.gant.trade.proxy.bytbit.v5.OrderApi;
import com.gant.trade.proxy.bytbit.v5.PositionApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class ByBitConfig {

    @Value("${app.bot.config.exchange.bybit.api.base-path}")
    private String basePath;

    @Bean
    @Primary
    public ApiClient byBitApiClient() {
        return new ApiClient().setBasePath(basePath);
    }

    @Bean
    @Primary
    public AssetApi byBitAssetApi(ApiClient byBitApiClient) {
        return new AssetApi(byBitApiClient);
    }


    @Bean
    @Primary
    public MarketApi byBitMarketApi(ApiClient byBitApiClient) {
        return new MarketApi(byBitApiClient);
    }

    @Bean
    @Primary
    public PositionApi byBitPositionApi(ApiClient byBitApiClient) {
        return new PositionApi(byBitApiClient);
    }

    @Bean
    @Primary
    public OrderApi byBitOrderApi(ApiClient byBitApiClient) {
        return new OrderApi(byBitApiClient);
    }
}
