package com.gant.trade.service.impl;

import com.gant.trade.domain.Strategy;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.mapper.StrategyMapper;
import com.gant.trade.domain.mapper.TradeMapper;
import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.rest.model.BotStrategy;
import com.gant.trade.rest.model.StrategyStatus;
import com.gant.trade.rest.model.StrategyStatusInfoTO;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.service.TradeStrategyService;
import com.gant.trade.utility.SymbolInfoUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import com.gant.trade.utility.constant.TelegramBotConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.toIntExact;

@Slf4j
@Service
@ConditionalOnProperty(value = "app.telegram.active", havingValue = "true")
public class TelegramBotServiceImpl extends TelegramLongPollingBot implements TelegramBotService {

    @Value("${app.telegram.username}")
    private String username;

    @Value("${app.telegram.token}")
    private String token;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    SymbolInfoUtil symbolInfoUtil;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            checkCommand(update);
        } else if (update.hasCallbackQuery()) {
            telegramCallback(update);
        }
    }

    private void telegramCallback(Update update) {
        String callData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        String[] message = callData.split(" ", 2);
        switch (message[0]) {
            case TelegramBotConstant.CALLBACK_STATUS_AUTOMATION: {
                Message messageUpdate = new Message();
                messageUpdate.setText(message[1]);
                messageUpdate.setChat(update.getCallbackQuery().getMessage().getChat());
                update.setMessage(messageUpdate);
                checkCommand(update);
                updateMessage("Strategy update", chatId, messageId);
                break;
            }
            case TelegramBotConstant.CALLBACK_CLOSE_ORDER: {
                Trade trade = tradeService.getTradeBySeqId(Long.valueOf(message[1]));
                SymbolInfoTO symbolInfo = symbolInfoUtil.getSymbolInfoByTrade(trade);
                boolean isStrategyStarted = false;
                for (TradeStrategyService tradeStrategyService : strategyService.getActiveTradeStrategyService()) {
                    if (trade.getStrategyId() == tradeStrategyService.getStrategyTO().getSeqId()) {
                        tradeStrategyService.closeOrderManually(symbolInfo, trade);
                        updateMessage("Closing Order", chatId, messageId);
                        isStrategyStarted = true;
                    }
                }
                if (!isStrategyStarted)
                    updateMessage("Strategy not started. Run Strategy and try again.", chatId, messageId);

                break;
            }
            case TelegramBotConstant.STRATEGY_STATUS_INFO: {
                List<StrategyStatusInfoTO> strategyStatusInfoTOList = strategyService.getStrategyStatusInfo(Long.parseLong(message[1]));
                String text = TradeStrategyServiceUtil.getStrategyStatusInfoMessage(strategyStatusInfoTOList);
                updateMessage(StringUtils.defaultIfBlank(text, "Strategy not started."), chatId, messageId);
                break;
            }
            case TelegramBotConstant.STRATEGY_TOTAL_GAIN: {
                double totalGain = tradeService.totalGain(Long.parseLong(message[1]));
                String text = String.format("Total Gain: %s", totalGain);
                updateMessage(text, chatId, messageId);
                break;
            }
            default:
                break;
        }
    }

    private void checkCommand(Update update) {
        try {
            String[] message = update.getMessage().getText().split(" ", 2);
            long chatId = update.getMessage().getChatId();
            switch (message[0]) {
                case TelegramBotConstant.START:
                    sendMessage(String.valueOf(chatId), String.format("Il tuo Chat ID:%n %s", chatId));
                    break;
                case TelegramBotConstant.HELP:
                    sendMessageToGanTradeBot(String.valueOf(chatId), String.join("\n", TelegramBotConstant.COMMAND_LIST));
                    break;
                case TelegramBotConstant.STATUS: {
                    StringBuilder text = new StringBuilder("I'm alive!");
                    strategyService.getActiveTradeStrategyService().forEach(tradeStrategyService -> {
                        text.append("\n").append(tradeStrategyService.getStrategyTO().getName()).append(tradeStrategyService.status() ? " is online." : " is offline.");
                    });
                    sendMessageToGanTradeBot(String.valueOf(chatId), text.toString());
                    break;
                }
                case TelegramBotConstant.AUTOMATION_START:
                    if (message.length > 1) {
                        String strategyId = message[1];
                        BotStrategy bot = new BotStrategy();
                        bot.setStrategySeqId(Long.valueOf(strategyId));
                        bot.setDebug(false);
                        strategyService.startBot(bot);
                    } else {
                        sendMessageToGanTradeBot(String.valueOf(chatId), "Error start strategy empty");
                    }
                    break;
                case TelegramBotConstant.AUTOMATION_STOP:
                    if (message.length > 1) {
                        String strategyId = message[1];
                        BotStrategy bot = new BotStrategy();
                        bot.setStrategySeqId(Long.valueOf(strategyId));
                        bot.setDebug(false);
                        strategyService.stopBot(bot);
                    } else {
                        sendMessageToGanTradeBot(String.valueOf(chatId), "Error stop strategy empty");
                    }
                    break;
                case TelegramBotConstant.AUTOMATION_LIST:
                    sendMessageButtonStrategy(String.valueOf(chatId), "Strategy List. Click button to Active/Deactive strategy", message[0]);
                    break;
                case TelegramBotConstant.OPEN_ORDER:
                    sendMessageButtonStrategy(String.valueOf(chatId), "Order List. Click button to close order manually", message[0]);
                    break;
                default:
                    log.debug("Not command found.");
            }
        } catch (Exception e) {
            log.error("Got exception", e);
        }
    }

    private void updateMessage(String text, long chatId, long messageId) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(toIntExact(messageId));
        newMessage.setText(text);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessageToGanTradeBot(String chatId, String text) {
        sendMessage(chatId, text);
    }

    @Override
    public void sendMessage(String chatId, String text) {
        try {
            if (chatId == null || chatId.isEmpty())
                return;

            SendMessage sendMessage = new SendMessage();
            sendMessage.enableHtml(true);
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);

            execute(sendMessage);
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse().contains("Can't access the chat") || e.getApiResponse().contains("Bot was blocked by the user")) {
                log.error("Failed to send message", e);
            }
        } catch (TelegramApiException e) {
            log.error("Failed to send message {} to {} due to error: {}", text, chatId, e.getMessage());
        }
    }

    @Override
    public void sendMessageButtonStrategy(String chatId, String text, String command) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            switch (command) {
                case TelegramBotConstant.AUTOMATION_LIST:
                    buildButtonListStrategy(rowsInline, chatId);
                    break;
                case TelegramBotConstant.OPEN_ORDER:
                    buildButtonListOrder(rowsInline, chatId);
                    break;
                default:
                    break;
            }

            markupInline.setKeyboard(rowsInline);
            sendMessage.setReplyMarkup(markupInline);

            execute(sendMessage);
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse().contains("Can't access the chat") || e.getApiResponse().contains("Bot was blocked by the user")) {
                log.error("Failed to send message", e);
            }
        } catch (TelegramApiException e) {
            log.error("Failed to send message {} to {} due to error: {}", text, chatId, e.getMessage());
        }

    }

    private void buildButtonListStrategy(List<List<InlineKeyboardButton>> rowsInline, String chatId) {
        List<Strategy> strategyList = strategyService.findStrategyByChatId(chatId);
        strategyList.forEach(s -> {
            StrategyStatus status = StrategyStatus.ACTIVE.equals(s.getStatus()) && strategyService.strategyStatus(s.getSeqId())
                    ? StrategyStatus.ACTIVE : StrategyStatus.DISABLE;

            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton strategyNameButton = new InlineKeyboardButton();
            strategyNameButton.setCallbackData(String.format("%s %s", TelegramBotConstant.STRATEGY_STATUS_INFO, s.getSeqId()));
            strategyNameButton.setText(s.getName());
            rowInline.add(strategyNameButton);

            InlineKeyboardButton strategyStartStopButton = new InlineKeyboardButton();
            strategyStartStopButton.setCallbackData(String.format("%s %s %s", TelegramBotConstant.CALLBACK_STATUS_AUTOMATION, StrategyStatus.ACTIVE.equals(status) ? TelegramBotConstant.AUTOMATION_STOP : TelegramBotConstant.AUTOMATION_START, s.getSeqId()));
            strategyStartStopButton.setText(StrategyStatus.ACTIVE.equals(status) ? "ACTIVATED" : "DISABLED");
            rowInline.add(strategyStartStopButton);

            InlineKeyboardButton totalGainButton = new InlineKeyboardButton();
            totalGainButton.setCallbackData(String.format("%s %s", TelegramBotConstant.STRATEGY_TOTAL_GAIN, s.getSeqId()));
            totalGainButton.setText("GAIN");
            rowInline.add(totalGainButton);

            rowsInline.add(rowInline);
        });
    }

    private void buildButtonListOrder(List<List<InlineKeyboardButton>> rowsInline, String chatId) {
        List<Trade> list = tradeService.getOpenOrderByChatId(chatId);

        list.forEach(trade -> {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData(String.format("%s %s", TelegramBotConstant.CALLBACK_CLOSE_ORDER, trade.getSeqId()));
            button.setText(String.format("%s - (%s)", trade.getSymbol(), trade.getStrategyId()));
            rowInline.add(button);
            rowsInline.add(rowInline);
        });
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
