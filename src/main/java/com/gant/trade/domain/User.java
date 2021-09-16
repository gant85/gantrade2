package com.gant.trade.domain;

import com.gant.trade.rest.model.ExchangeConfiguration;
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
public class User {

    @Transient
    public static final String SEQUENCE_NAME = "user_sequence";

    @Id
    private String id;
    private long seqId;
    private String keycloakId;
    private String name;
    private String surname;
    private String email;
    private String telegramId;
    private Date insertionTime;
    private Date updateTime;
    private List<ExchangeConfiguration> exchangeConfiguration = new ArrayList<>();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
