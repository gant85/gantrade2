package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
public class Rule {
    @Id
    private String id;
    private RuleType type;
    private Condition condition;
    private Action action;
    private Operator operator;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

