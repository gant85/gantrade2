package com.gant.trade.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandlestickEvent {
    private String eventType;
    private long eventTime;
    private String symbol;
    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
    private String intervalId;
    private Long firstTradeId;
    private Long lastTradeId;
    private String quoteAssetVolume;
    private Long numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
    private Boolean isBarFinal;
}