package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter
@Setter
@Document
public class Action {
    @Id
    private String id;
    private ActionTypeEnum type;
    private BigDecimal amount;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}