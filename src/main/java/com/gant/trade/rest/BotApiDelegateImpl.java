package com.gant.trade.rest;


import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.rest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BotApiDelegateImpl implements BotApiDelegate {

    @Autowired
    private StrategyService strategyService;

    @Override
    public ResponseEntity<Void> botStatus() {
        log.info("Starting interaction: botStatus");
        ResponseEntity<Void> response = strategyService.botStatus()
                ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        log.info("End interaction: botStatus");
        return response;
    }

    @Override
    public ResponseEntity<BotStrategyCreateResponse> createStrategy(BotStrategyCreateRequest botStrategyCreateRequest) {
        log.info("Starting interaction: createStrategy");
        StrategyTO strategyTO = strategyService.createStrategy(botStrategyCreateRequest);
        BotStrategyCreateResponse botStrategyCreateResponse = new BotStrategyCreateResponse();
        botStrategyCreateResponse.setId(strategyTO.getSeqId());
        log.info("End interaction: createStrategy");
        return ResponseEntity.ok(botStrategyCreateResponse);
    }

    @Override
    public ResponseEntity<Void> deleteStrategyById(Long id) {
        strategyService.deleteStrategyById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StrategyTO> getStrategyById(Long id) {
        log.info("Starting interaction: getStrategyById");
        StrategyTO strategy = strategyService.getStrategyById(id);
        log.info("End interaction: getStrategyById");
        if (strategy != null) {
            return ResponseEntity.ok(strategy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> startBot(BotStrategy botStrategy) {
        log.info("Starting interaction: startBot strategySeqId={} strategyName={} debug={}", botStrategy.getStrategySeqId(), botStrategy.getStrategyName(), botStrategy.getDebug());
        strategyService.startBot(botStrategy);
        log.info("End interaction: startBot strategySeqId={} strategyName={} debug={}", botStrategy.getStrategySeqId(), botStrategy.getStrategyName(), botStrategy.getDebug());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> stopBot(BotStrategy botStrategy) {
        log.info("Starting interaction: stopBot strategySeqId={} strategyName={}", botStrategy.getStrategySeqId(), botStrategy.getStrategyName());
        if (strategyService.stopBot(botStrategy)) {
            log.info("End interaction: stopBot strategySeqId={} strategyName={}", botStrategy.getStrategySeqId(), botStrategy.getStrategyName());
            return ResponseEntity.noContent().build();
        }
        log.info("End interaction: stopBot strategySeqId={} strategyName={}", botStrategy.getStrategySeqId(), botStrategy.getStrategyName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public ResponseEntity<StrategyListTO> strategyList(Integer userId, Integer pageSize, Integer pageIndex) {
        log.info("Starting interaction: strategyList");
        StrategyListTO strategyListTO = strategyService.strategyList(userId, pageSize, pageIndex);
        log.info("End interaction: strategyList");
        return ResponseEntity.ok(strategyListTO);
    }

    @Override
    public ResponseEntity<StrategySimulationResponse> strategySimulation(StrategySimulationRequest strategySimulationRequest) {
        log.info("Starting interaction: strategySimulation");
        StrategySimulationResponse strategySimulationResponse = strategyService.strategySimulation(strategySimulationRequest);
        log.info("End interaction: strategySimulation");
        return ResponseEntity.ok(strategySimulationResponse);
    }

    @Override
    public ResponseEntity<StrategyTO> updateStrategyById(Long id, StrategyTO strategyTO) {
        log.info("Starting interaction: updateStrategy id={}", id);
        StrategyTO savedStrategyTO = strategyService.updateStrategyById(id, strategyTO);
        log.info("End interaction: updateStrategy id={}", id);
        return ResponseEntity.ok(savedStrategyTO);
    }

    @Override
    public ResponseEntity<Void> botStrategyStatus(Long id) {
        log.info("Starting interaction: botStrategyStatus id={}", id);
        ResponseEntity<Void> response = strategyService.strategyStatus(id)
                ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        log.info("End interaction: botStrategyStatus id={}", id);
        return response;
    }

    @Override
    public ResponseEntity<List<StrategyStatusInfoTO>> getStrategyStatusInfo(Long id) {
        log.info("Starting interaction: getStrategyStatusInfo id={}", id);
        List<StrategyStatusInfoTO> strategyStatusInfoTOList = strategyService.getStrategyStatusInfo(id);
        log.info("End interaction: getStrategyStatusInfo id={}", id);
        return ResponseEntity.ok(strategyStatusInfoTOList);
    }
}
