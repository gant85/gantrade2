package com.gant.trade.domain;

import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.SymbolInfoTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document
public class Order {

    @Transient
    public static final String SEQUENCE_NAME = "order_sequence";

    @Id
    private String id;
    private long seqId;
    private SymbolInfoTO symbolInfo;
    private String type;
    private String side;
    private double price;
    private double amount;
    private double balance;
    private double priceAuxLimit = 0;
    private double priceTrailing = 0;
    private long clientOrderId;
    private Date insertionTime;
    private Date updateTime;
    private List<Bar> bars;
    private Exchange exchange;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

