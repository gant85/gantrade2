package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.constant.BinanceApiConstants;
import com.gant.binance.api.client.domain.OrderSide;
import com.gant.binance.api.client.domain.OrderType;
import com.gant.binance.api.client.domain.account.Account;
import com.gant.binance.api.client.domain.account.NewOrder;
import com.gant.binance.api.client.domain.account.NewOrderResponse;
import com.gant.trade.domain.Order;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.mongo.repository.OrderRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.rest.model.TradeState;
import com.gant.trade.service.OrderService;
import com.gant.trade.service.TelegramBotService;
import com.gant.trade.utility.BarSeriesUtil;
import com.gant.trade.utility.DecimalFormatUtil;
import com.gant.trade.utility.SymbolInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class OrderBinanceService implements OrderService<BinanceApiRestClient> {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SymbolInfoUtil symbolInfoUtil;

    double feeBinance = 1.001;

    @Override
    public void openTrade(BinanceApiRestClient binanceApiRestClient, Trade trade, Bar bar, double orderSize, boolean debug) {
        String chatId = null;
        SymbolInfo symbolInfo = null;
        try {
            trade.setTradeState(TradeState.OPENING);

            User user = userRepository.findBySeqId(trade.getUserId());
            if (user != null) {
                chatId = user.getTelegramId();
            }

            symbolInfo = symbolInfoUtil.getSymbolInfoByExchange(trade.getExchange(), trade.getUserId(), trade.getSymbol(), orderSize);

            double price = Double.parseDouble(binanceApiRestClient.getPrice(symbolInfo.getSymbol()).getPrice());
            double amount = symbolInfoUtil.getAmount(price, symbolInfo);

            NewOrder newOrder = new NewOrder(symbolInfo.getSymbol(), com.gant.binance.api.client.domain.OrderSide.BUY, OrderType.MARKET, null, String.valueOf(amount));

            NewOrderResponse newOrderResponse;
            if (debug) {
                binanceApiRestClient.newOrderTest(newOrder);
                newOrderResponse = new NewOrderResponse();
                newOrderResponse.setExecutedQty(newOrder.getQuantity());
                newOrderResponse.setPrice(String.valueOf(price));
                newOrderResponse.setOrderId(1L);
            } else {
                newOrderResponse = binanceApiRestClient.newOrder(newOrder);
            }
            log.debug("price={} newOrderResponse={}", price, newOrderResponse.toString());

            Order order = new Order();
            order.setSymbolInfo(symbolInfo);
            order.setType(OrderType.MARKET.name());
            order.setSide(OrderSide.BUY.name());
            order.setPrice(price);
            order.setAmount(Double.parseDouble(newOrderResponse.getExecutedQty()));
            order.setClientOrderId(newOrderResponse.getOrderId());
            order.setBars(Collections.singletonList(BarSeriesUtil.convert(bar)));
            order.setInsertionTime(new Date());

            trade.getOrders().add(order);
            trade.setAmount(amount);
            trade.setExpectedPriceOpen(order.getPrice());
            trade.setTradeState(TradeState.OPEN);
            trade.setInsertionTime(new Date());

            long start = System.currentTimeMillis();
            long serverTime = binanceApiRestClient.getServerTime();
            long end = System.currentTimeMillis();
            long gap = end - start;

            Account account = binanceApiRestClient.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, serverTime + gap);
            String balanceBaseAsset = account.getAssetBalance(symbolInfo.getQuoteAsset()).getFree();
            order.setBalance(Double.parseDouble(balanceBaseAsset));

            String message = String.format(
                    "%s%n%s %s",
                    order.getSide(),
                    symbolInfo.getBaseAsset(), BigDecimal.valueOf(order.getPrice())
            );
            log.info(message.replace("\n", " - ").replace("<b>", ""));
            telegramBotService.sendMessageToGanTradeBot(chatId, message);
        } catch (Exception e) {
            String message = String.format("Got an exception while opening trade. Error: %s", e.getMessage());
            log.error(message, e);
            telegramBotService.sendMessageToGanTradeBot(chatId, message);
            trade.setTradeState(TradeState.ERROR);
        } finally {
            Trade tradeSaved = tradeService.save(trade);
            trade.setId(tradeSaved.getId());
        }
    }

    @Override
    public void closeTrade(final BinanceApiRestClient binanceApiRestClient, Trade trade, Bar bar, double orderSize, boolean debug) {
        String chatId = null;
        try {
            User user = userRepository.findBySeqId(trade.getUserId());
            if (user != null) {
                chatId = user.getTelegramId();
            }

            Order buyOrder = trade.getOrders().get(0);
            SymbolInfo symbolInfo = symbolInfoUtil.getSymbolInfoByExchange(trade.getExchange(), trade.getUserId(), trade.getSymbol(), orderSize);
            double price = Double.parseDouble(binanceApiRestClient.getPrice(symbolInfo.getSymbol()).getPrice());
            long start = System.currentTimeMillis();
            long serverTime = binanceApiRestClient.getServerTime();
            long end = System.currentTimeMillis();
            long gap = end - start;

            Account account = binanceApiRestClient.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, serverTime + gap);

            String preActionBalanceWalletCurrency = account.getAssetBalance(symbolInfo.getQuoteAsset()).getFree();
            double baseAssetAmount = Double.parseDouble(account.getAssetBalance(symbolInfo.getBaseAsset()).getFree());
            double buyOrderAmount = buyOrder.getAmount();

            //if the expected quantity(buyOrderAmount) is less than the available quantity(baseAssetAmount), sell the available quantity
            double amountOrder = (buyOrderAmount < baseAssetAmount) ? buyOrderAmount : baseAssetAmount / feeBinance;
            double amount = symbolInfoUtil.getAmountPrecision(amountOrder, symbolInfo);

            NewOrder newOrder = new NewOrder(symbolInfo.getSymbol(), com.gant.binance.api.client.domain.OrderSide.SELL, OrderType.MARKET, null, String.valueOf(amount));

            NewOrderResponse newOrderResponse;
            if (debug) {
                binanceApiRestClient.newOrderTest(newOrder);
                newOrderResponse = new NewOrderResponse();
                newOrderResponse.setExecutedQty(newOrder.getQuantity());
                newOrderResponse.setPrice(String.valueOf(price));
                newOrderResponse.setOrderId(2L);
            } else {
                newOrderResponse = binanceApiRestClient.newOrder(newOrder);
            }
            log.debug("price={} newOrderResponse={}", price, newOrderResponse.toString());

            Order order = new Order();
            order.setSymbolInfo(symbolInfo);
            order.setType(OrderType.MARKET.name());
            order.setSide(OrderSide.SELL.name());
            order.setPrice(price);
            order.setAmount(Double.parseDouble(newOrderResponse.getExecutedQty()));
            order.setClientOrderId(newOrderResponse.getOrderId());
            order.setBars(Collections.singletonList(BarSeriesUtil.convert(bar)));
            order.setInsertionTime(new Date());

            trade.getOrders().add(order);
            trade.setExpectedPriceClose(order.getPrice());
            trade.setTradeState(TradeState.CLOSED);
            trade.setGain((trade.getExpectedPriceClose() - trade.getExpectedPriceOpen()) * trade.getAmount());

            account = binanceApiRestClient.getAccount(BinanceApiConstants.DEFAULT_RECEIVING_WINDOW, serverTime + gap);

            String balanceWalletCurrency = account.getAssetBalance(symbolInfo.getQuoteAsset()).getFree();
            order.setBalance(Double.parseDouble(balanceWalletCurrency));

            double spent = order.getBalance() - Double.parseDouble(preActionBalanceWalletCurrency);
            order.setPriceTrailing(spent);

            double gain = (order.getPrice() - buyOrder.getPrice()) * amount;
            trade.setGain(gain);

            double percentage = (trade.getExpectedPriceClose() - trade.getExpectedPriceOpen()) / trade.getExpectedPriceOpen() * 100;
            trade.setPercentage(DecimalFormatUtil.round(percentage, 2));

            String message = String.format(
                    "%s%n%s %s%ngain: %s <b>%s</b>%nspent: %s %s",
                    order.getSide(),
                    symbolInfo.getBaseAsset(), BigDecimal.valueOf(order.getPrice()),
                    symbolInfo.getQuoteAsset(), DecimalFormatUtil.format(gain),
                    symbolInfo.getQuoteAsset(), DecimalFormatUtil.format(spent)
            );
            log.info(message.replace("\n", " - ").replace("<b>", "").replace("</b>", ""));
            telegramBotService.sendMessageToGanTradeBot(chatId, message);
        } catch (Exception e) {
            String message = String.format("Got an exception while closing trade %s. Error: %s", trade.getId(), e.getMessage());
            log.error(message, e);
            telegramBotService.sendMessageToGanTradeBot(chatId, message);
            trade.setTradeState(TradeState.ERROR);
        } finally {
            tradeService.save(trade);
        }
    }
}
