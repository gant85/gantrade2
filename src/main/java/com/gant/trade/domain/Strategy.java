package com.gant.trade.domain;

import com.gant.trade.rest.model.Exchange;
import com.gant.trade.rest.model.StrategyStatus;
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
public class Strategy {

    @Transient
    public static final String SEQUENCE_NAME = "strategy_sequence";

    @Id
    private String id;
    private long seqId;
    private String name;
    private List<SymbolInfo> symbolInfo = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private TimeFrame timeframe;
    private boolean checkRulesEveryTime = true;
    private int checkRulesEveryTimeValue = 1;
    private Date insertionTime;
    private Date updateTime;
    private StrategyStatus status;
    private long userId;
    private Exchange exchange;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

