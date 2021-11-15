package com.gant.trade.mongo.service;

import com.gant.trade.config.BotConfig;
import com.gant.trade.rest.model.BotStrategy;
import com.gant.trade.rest.model.UserTO;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.service.TradeStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TradeStrategySchedulerService {

    @Autowired
    private BotConfig botConfig;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private UserService userService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Scheduled(fixedRate = 60000)
    public void checkCandlestickSubscriptions() {
        for (TradeStrategyService tradeStrategyService : strategyService.getActiveTradeStrategyService()) {
            if (tradeStrategyService.getCandlestickEventLastTimes() != null && !tradeStrategyService.getCandlestickEventLastTimes().isEmpty()) {
                boolean isNotAlive = false;
                for (String symbolInfo : tradeStrategyService.getCandlestickEventLastTimes().keySet()) {
                    LocalDateTime candlestickEventLastTime = tradeStrategyService.getCandlestickEventLastTimes().get(symbolInfo);
                    log.debug("{} checkCandlestickSubscriptions: symbolInfo={} candlestickEventLastTime={}", tradeStrategyService.getStrategyTO().getName(), symbolInfo, candlestickEventLastTime.format(DateTimeFormatter.ISO_DATE_TIME));
                    if (!isNotAlive) {
                        isNotAlive = Duration.between(candlestickEventLastTime, LocalDateTime.now()).toMinutes() > botConfig.getHealthCheck();
                    }
                }
                if (isNotAlive) {
                    String message = String.format("%s sleepy. Restart bot.", tradeStrategyService.getStrategyTO().getName());

                    UserTO user = userService.getUserById(tradeStrategyService.getStrategyTO().getUserId());
                    telegramBotService.sendMessageToGanTradeBot(user.getTelegramId(), message);
                    log.warn(message);
                    BotStrategy botStrategy = new BotStrategy();
                    botStrategy.setStrategyName(tradeStrategyService.getStrategyTO().getName());
                    botStrategy.setDebug(tradeStrategyService.isDebug());
                    strategyService.stopBot(botStrategy);
                    strategyService.startBot(botStrategy);
                }
            }
        }
    }
}
