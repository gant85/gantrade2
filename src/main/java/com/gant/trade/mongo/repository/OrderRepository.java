package com.gant.trade.mongo.repository;

import com.gant.trade.domain.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, Long> {

    Order findBySeqId(long seqId);
}
