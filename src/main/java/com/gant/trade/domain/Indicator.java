package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
public class Indicator {
    @Id
    private String id;
    private Long period;
    private IndicatorType type;
    private IndicatorType indicatorTypeRef;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

