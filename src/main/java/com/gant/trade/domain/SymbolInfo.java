package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
public class SymbolInfo {

    @Transient
    public static final String SEQUENCE_NAME = "symbol_sequence";

    private String symbol;
    private String baseAsset;
    private Integer baseAssetPrecision;
    private String quoteAsset;
    private Integer quoteAssetPrecision;
    private String stepSize;
    private String minQty;
    private String tickSizePrice;
    private double orderSize;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
