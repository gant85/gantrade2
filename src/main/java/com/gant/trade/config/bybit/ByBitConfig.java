package com.gant.trade.config.bybit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ByBitConfig {
    private ByBitApiConfig api;
    private ByBitWebSocketConfig webSocket;
}