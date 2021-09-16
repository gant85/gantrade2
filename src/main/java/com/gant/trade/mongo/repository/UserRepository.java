package com.gant.trade.mongo.repository;

import com.gant.trade.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, Long> {

    User findBySeqId(long seqId);

    User findByEmail(String email);

    User findByTelegramId(String telegramId);
}
