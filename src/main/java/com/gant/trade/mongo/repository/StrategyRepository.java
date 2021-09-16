package com.gant.trade.mongo.repository;

import com.gant.trade.domain.Strategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StrategyRepository extends MongoRepository<Strategy, Long> {

    Strategy findByName(String name);

    Strategy findBySeqId(long seqId);

    List<Strategy> findByUserId(long userId);

    Page<Strategy> findByUserId(long userId, Pageable pageable);

    List<Strategy> findByStatus(String status);
}
