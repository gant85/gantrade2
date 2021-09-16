package com.gant.trade.rest;

import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.rest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradesApiDelegateImpl implements TradesApiDelegate {

    @Autowired
    private TradeService tradeService;

    @Override
    public ResponseEntity<Void> deleteTradeById(Long id) {
        log.info("Starting interaction: deleteTradeById");
        tradeService.deleteTradeById(id);
        log.info("End interaction: deleteTradeById");
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TradeTO> getTradeById(Long id) {
        log.info("Starting interaction: getTradeById");
        TradeTO trade = tradeService.getTradeTOBySeqId(id);
        log.info("End interaction: getTradeById");
        if (trade != null) {
            return ResponseEntity.ok(trade);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<TradeListTO> tradeList(Integer userId, String tradeState, String tradeDirection, String symbol, Integer pageSize, Integer pageIndex) {
        log.info("Starting interaction: tradeList");
        TradeListTO tradeListTO = tradeService.tradeList(userId, pageSize, pageIndex);
        log.info("End interaction: tradeList");
        return ResponseEntity.ok(tradeListTO);
    }

    @Override
    public ResponseEntity<TradeTO> updateTradeById(Long id, TradeTO tradeTO) {
        log.info("Starting interaction: updateTradeById");
        TradeTO savedTrade = tradeService.updateTradeById(id, tradeTO);
        log.info("End interaction: updateTradeById");
        return ResponseEntity.ok(savedTrade);
    }
}
