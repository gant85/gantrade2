package com.gant.trade.service;

public interface TelegramBotService {

    void sendMessageToGanTradeBot(String chatId,String text);

    void sendMessage(String chatId, String text);

    void sendMessageButtonStrategy(String chatId, String text,String command);
}
