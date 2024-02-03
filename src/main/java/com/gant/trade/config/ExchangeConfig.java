package com.gant.trade.config;

import com.gant.trade.config.bybit.ByBitConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ExchangeConfig {
    private ByBitConfig bybit;
}
