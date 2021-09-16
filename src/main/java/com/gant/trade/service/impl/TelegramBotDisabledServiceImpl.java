package com.gant.trade.service.impl;

import com.gant.trade.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "app.telegram.active", havingValue = "false")
public class TelegramBotDisabledServiceImpl implements TelegramBotService {

    @Override
    public void sendMessageToGanTradeBot(String chatId, String text) {
        log.info("No Telegram sendMessageToGanTradeBot");
    }

    @Override
    public void sendMessage(String chatId, String text) {
        log.info("No Telegram sendMessage");
    }

    @Override
    public void sendMessageButtonStrategy(String chatId, String text,String command) { log.info("No Telegram sendMessageButtonStrategy"); }
}

