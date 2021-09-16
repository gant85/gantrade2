package com.gant.trade.mongo.repository;

import com.gant.trade.domain.Trade;
import com.gant.trade.rest.model.TradeState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeRepository extends MongoRepository<Trade, Long> {

    Trade findBySeqId(long seqId);

    List<Trade> findByTradeState(TradeState tradeState);

    List<Trade> findByTradeStateAndStrategyIdIn(TradeState open, List<Long> ids);

    Page<Trade> findByUserId(long userId, Pageable pageable);
}
