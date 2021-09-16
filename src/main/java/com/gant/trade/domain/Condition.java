package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Document
public class Condition {
    @Id
    private String id;
    private ConditionType type;
    private BigDecimal threshold;
    private List<Indicator> indicators;
    private Operator operator;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

