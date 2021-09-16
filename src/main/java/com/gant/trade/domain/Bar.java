package com.gant.trade.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document
public class Bar {

    @Id
    private String id;
    private double openPrice;
    private double minPrice;
    private double maxPrice;
    private double closePrice;
    private double volume;
    private long trades;
    private double amount;
    private long timePeriod;
    private Date beginTime;
    private Date endTime;
    private Date insertionTime;
    private Date updateTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
