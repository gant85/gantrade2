package com.gant.trade.mongo.service;

import com.gant.trade.domain.Strategy;
import com.gant.trade.domain.Trade;
import com.gant.trade.domain.User;
import com.gant.trade.domain.mapper.OrderMapper;
import com.gant.trade.domain.mapper.TradeMapper;
import com.gant.trade.mongo.repository.StrategyRepository;
import com.gant.trade.mongo.repository.TradeRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.rest.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StrategyRepository strategyRepository;

    public Map<String, List<Trade>> getOpenTradesByStrategy(long stratedyId) {
        Map<String, List<Trade>> trades = new HashMap<>();

        List<Long> ids = Collections.singletonList(stratedyId);
        tradeRepository.findByTradeStateAndStrategyIdIn(TradeState.OPEN, ids).forEach(trade -> {
            addTradeToOpenTradeList(trades, trade);
        });

        return trades;
    }

    public double totalGain(Long strategyId) {
        List<Trade> list = tradeRepository.findByStrategyId(strategyId);

        return list.stream().filter(t -> t.getTradeState().equals(TradeState.CLOSED)).map(Trade::getGain)
                .mapToDouble(Double::doubleValue).sum();
    }

    public void removeTradeToOpenTradeList(Map<String, List<Trade>> trades, Trade trade) {
        List<Trade> list = trades.get(trade.getSymbol());
        if (list != null) {
            list.removeIf(t -> t.getId().equals(trade.getId()));
        }
    }

    public Trade save(Trade trade) {
        return tradeRepository.save(trade);
    }

    public TradeTO getTradeTOBySeqId(Long id) {
        Trade trade = tradeRepository.findBySeqId(id);
        return tradeMapper.convert(trade);
    }

    public Trade getTradeBySeqId(Long id) {
        return tradeRepository.findBySeqId(id);
    }

    public void deleteTradeById(Long id) {
        tradeRepository.deleteById(id);
    }

    public TradeTO updateTradeById(Long id, TradeTO tradeTO) {
        Trade trade = tradeRepository.findById(id).orElse(null);
        if (trade != null) {
            trade.setTradeState(tradeTO.getTradeState());
            Trade tradeSaved = tradeRepository.save(trade);
            return tradeMapper.convert(tradeSaved);
        }
        return null;
    }

    public List<Trade> getOpenOrderByChatId(String chatId) {
        User user = userRepository.findByTelegramId(chatId);
        if (user != null) {
            List<Strategy> strategyList = strategyRepository.findByUserId(user.getSeqId());
            List<Long> ids = strategyList.stream().map(Strategy::getSeqId).collect(Collectors.toList());
            return tradeRepository.findByTradeStateAndStrategyIdIn(TradeState.OPEN, ids);
        } else {
            return new ArrayList<>();
        }
    }

    public void addTradeToOpenTradeList(Map<String, List<Trade>> trades, Trade trade) {
        if (!trades.containsKey(trade.getSymbol())) {
            trades.put(trade.getSymbol(), new ArrayList<>());
        }
        trades.get(trade.getSymbol()).add(trade);
    }

    public TradeListTO tradeList(Integer userId, Integer pageSize, Integer pageIndex) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Trade> page = tradeRepository.findByUserId(userId, pageable);
        Pagination pagination = new Pagination();
        pagination.setPageSize(page.getSize());
        pagination.setIsLastPage(!page.hasNext());
        pagination.setNextPageIndex(page.hasNext() ? String.valueOf(page.getNumber() + 1) : null);
        pagination.setTotalItems(Math.toIntExact(page.getTotalElements()));
        TradeListTO tradeListTO = new TradeListTO();
        tradeListTO.setPagination(pagination);
        tradeListTO.setTrades(tradeMapper.convertList(page.getContent()));
        return tradeListTO;
    }

    public List<OrderTO> getOpenOrderByCurrencyAndStrategySeqId(String currency, Long seqId) {
        Map<String, List<Trade>> currencyTradeMap = getOpenTradesByStrategy(seqId);
        if (currencyTradeMap.isEmpty()) {
            return Collections.emptyList();
        }

        return currencyTradeMap.get(currency).stream().map(Trade::getOrders).flatMap(Collection::stream).map(order -> orderMapper.convert(order)).collect(Collectors.toList());
    }
}
