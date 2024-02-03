package com.gant.trade.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = "app.bot.config")
public class BotConfig {
    private Integer healthCheck = 14;
    private Integer retryInSecond = 1;
    private Integer maxRetry = 3;
    private ExchangeConfig exchange;
}
