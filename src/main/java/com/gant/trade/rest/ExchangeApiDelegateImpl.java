package com.gant.trade.rest;


import com.gant.trade.mongo.service.UserService;
import com.gant.trade.rest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExchangeApiDelegateImpl implements ExchangeApiDelegate {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Void> addExchange(Long userId, ExchangeConfiguration exchangeConfiguration) {
        log.info("Starting interaction: addExchange");
        userService.addUserExchange(userId,exchangeConfiguration);
        log.info("End interaction: addExchange");
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteExchangeByUserId(Long userId,String exchange) {
        log.info("Starting interaction: deleteExchangeByUserId");
        userService.deleteUserExchange(userId,exchange);
        log.info("End interaction: deleteExchangeByUserId");
        return ResponseEntity.noContent().build();
    }

}
