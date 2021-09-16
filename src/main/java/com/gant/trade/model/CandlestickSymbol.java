package com.gant.trade.model;

public class CandlestickSymbol {

    private String symbolInfo;
    private Timeframe timeframe;

    public CandlestickSymbol(String symbolInfo, Timeframe timeframe) {
        this.symbolInfo = symbolInfo;
        this.timeframe = timeframe;
    }

    public String getSymbolInfo() {
        return this.symbolInfo;
    }

    public Timeframe getTimeframe() {
        return this.timeframe;
    }
}
