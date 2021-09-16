package com.gant.trade.domain;

import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.TradeState;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document
public class Trade {

    @Transient
    public static final String SEQUENCE_NAME = "trade_sequence";

    @Id
    private String id;
    private long seqId;
    private long strategyId;
    private long userId;
    private Exchange exchange;
    private TradeState tradeState;
    private String tradeDirection;
    private List<Order> orders;
    private String symbol;
    private double amount;
    private double expectedPriceOpen;
    private double expectedPriceClose;
    private double gain;
    private double percentage;
    private Date insertionTime;
    private Date updateTime;

    public Trade() {
        tradeState = TradeState.CREATED;
        orders = new ArrayList<>();
        expectedPriceOpen = -1;
        expectedPriceClose = -1;
    }

    public Trade(final long strategyId, final long userId, final Exchange exchange, final String tradeDirection, final String symbol, final double amount) {
        this();
        this.strategyId = strategyId;
        this.userId = userId;
        this.exchange = exchange;
        this.tradeDirection = tradeDirection;
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
