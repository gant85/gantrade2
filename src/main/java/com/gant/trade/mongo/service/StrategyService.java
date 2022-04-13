package com.gant.trade.mongo.service;

import com.gant.trade.domain.Strategy;
import com.gant.trade.domain.User;
import com.gant.trade.domain.mapper.StrategyMapper;
import com.gant.trade.exception.StrategyAlreadyExistException;
import com.gant.trade.exception.StrategyNotFoundException;
import com.gant.trade.mongo.repository.StrategyRepository;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.rest.model.*;
import com.gant.trade.service.TradeStrategyService;
import com.gant.trade.service.impl.binance.StatusInfoService;
import com.gant.trade.service.impl.binance.TradeStrategyBinanceService;
import com.gant.trade.service.impl.binance.TradeStrategyBinanceSimulationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
@Transactional
public class StrategyService {

    @Autowired
    private TradeStrategyBinanceSimulationService tradeStrategySimulationService;
    @Autowired
    private StrategyRepository strategyRepository;
    @Autowired
    private StrategyMapper strategyMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StatusInfoService statusInfoService;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private static final Map<Long, TradeStrategyService> tradeStrategyServiceMap = new HashMap<>();

    public boolean botStatus() {
        return !tradeStrategyServiceMap.isEmpty() && tradeStrategyServiceMap.values().stream().allMatch(TradeStrategyService::status);
    }

    public boolean strategyStatus(Long strategyId) {
        TradeStrategyService tradeStrategyService = tradeStrategyServiceMap.get(strategyId);
        return tradeStrategyService != null && tradeStrategyService.status();
    }

    public List<StrategyStatusInfoTO> getStrategyStatusInfo(Long id) {
        TradeStrategyService tradeStrategyService = tradeStrategyServiceMap.get(id);
        if (tradeStrategyService != null && tradeStrategyService.status()) {
            return tradeStrategyService.getStrategyStatusInfoToList();
        } else {
            return statusInfoService.getStrategyStatusInfoToList(id);
        }
    }

    public Collection<TradeStrategyService> getActiveTradeStrategyService() {
        if (tradeStrategyServiceMap.isEmpty()) {
            return Collections.emptyList();
        }
        return tradeStrategyServiceMap.values();
    }

    public StrategyTO createStrategy(BotStrategyCreateRequest botStrategyCreateRequest) {
        if (getStrategyByName(botStrategyCreateRequest.getStrategy().getName()) != null) {
            throw new StrategyAlreadyExistException();
        }
        Strategy strategy = strategyMapper.convert(botStrategyCreateRequest.getStrategy());
        Strategy strategySaved = strategyRepository.save(strategy);
        return strategyMapper.convert(strategySaved);
    }

    public void deleteStrategyById(Long seqId) {
        stopIsRunning(seqId);
        Strategy strategy = strategyRepository.findBySeqId(seqId);
        strategyRepository.delete(strategy);
    }

    public StrategyTO getStrategyById(Long seqId) {
        Strategy strategy = strategyRepository.findBySeqId(seqId);
        return strategyMapper.convert(strategy);
    }

    private StrategyTO getStrategyByName(String name) {
        Strategy strategy = strategyRepository.findByName(name);
        return strategyMapper.convert(strategy);
    }

    public void startBot(BotStrategy botStrategy) {
        Strategy strategy = getStrategy(botStrategy);
        // TODO rimuovere in futuro, troppe strategie non vanno bene nell'hashmap
        TradeStrategyService tradeStrategyService = tradeStrategyServiceMap.get(strategy.getSeqId());
        if (tradeStrategyService != null) {
            tradeStrategyService.stop();
            tradeStrategyServiceMap.remove(strategy.getSeqId());
        }

        runStrategy(botStrategy, strategy);
        strategy.setStatus(StrategyStatus.ACTIVE);
        strategyRepository.save(strategy);
    }

    private void runStrategy(BotStrategy botStrategy, Strategy strategy) {
        switch (strategy.getExchange()) {
            case BINANCE:
                TradeStrategyService tradeStrategyService = new TradeStrategyBinanceService(applicationContext, botStrategy.getDebug());
                User user = userRepository.findBySeqId(strategy.getUserId());
                tradeStrategyService.start(strategyMapper.convert(strategy), user);
                tradeStrategyServiceMap.put(strategy.getSeqId(), tradeStrategyService);
                break;
            case COINBASE:
                break;
        }
    }

    public boolean stopBot(BotStrategy botStrategy) {
        boolean stopped = false;
        Strategy strategy = getStrategy(botStrategy);
        TradeStrategyService tradeStrategyService = tradeStrategyServiceMap.get(strategy.getSeqId());
        if (tradeStrategyService != null) {
            stopped = tradeStrategyService.stop();
            tradeStrategyServiceMap.remove(strategy.getSeqId());
        }
        if (stopped) {
            strategy.setStatus(StrategyStatus.DISABLE);
            strategyRepository.save(strategy);
        }
        return stopped;
    }

    private Strategy getStrategy(BotStrategy botStrategy) {
        Strategy strategy = null;
        if (botStrategy.getStrategySeqId() != null) {
            strategy = strategyRepository.findBySeqId(botStrategy.getStrategySeqId());
        } else if (botStrategy.getStrategyName() != null) {
            strategy = strategyRepository.findByName(botStrategy.getStrategyName());
        }
        if (strategy == null) {
            throw new StrategyNotFoundException();
        }
        return strategy;
    }

    public StrategyListTO strategyList(Integer userId, Integer pageSize, Integer pageIndex) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Strategy> page = strategyRepository.findByUserId(userId, pageable);
        Pagination pagination = new Pagination();
        pagination.setPageSize(page.getSize());
        pagination.setIsLastPage(!page.hasNext());
        pagination.setNextPageIndex(page.hasNext() ? String.valueOf(page.getNumber() + 1) : null);
        pagination.setTotalItems(Math.toIntExact(page.getTotalElements()));
        StrategyListTO strategyListTO = new StrategyListTO();
        strategyListTO.setPagination(pagination);
        strategyListTO.setStrategies(strategyMapper.convertList(page.getContent()));
        return strategyListTO;
    }

    public StrategySimulationResponse strategySimulation(StrategySimulationRequest strategySimulationRequest) {
        switch (strategySimulationRequest.getExchange()) {
            case BINANCE:
                return tradeStrategySimulationService.simulation(strategySimulationRequest);
            case COINBASE:
                return null;
            default:
                return null;
        }
    }

    public StrategyTO updateStrategyById(Long id, StrategyTO strategyTO) {
        stopIsRunning(id);
        Strategy strategy = strategyRepository.findBySeqId(id);
        if (strategy == null) {
            throw new StrategyNotFoundException();
        } else if (!strategyTO.getName().equalsIgnoreCase(strategy.getName()) && getStrategyByName(strategyTO.getName()) != null) {
            throw new StrategyAlreadyExistException();
        }

        Strategy newStrategy = strategyMapper.convert(strategyTO);
        newStrategy.setSeqId(id);
        newStrategy.setInsertionTime(strategy.getInsertionTime());
        Strategy strategySaved = strategyRepository.save(newStrategy);
        return strategyMapper.convert(strategySaved);
    }

    private void stopIsRunning(Long strategyId) {
        TradeStrategyService tradeStrategyService = tradeStrategyServiceMap.get(strategyId);
        if (tradeStrategyService != null) {
            tradeStrategyService.stop();
            tradeStrategyServiceMap.remove(strategyId);
        }
    }

    public List<Strategy> findByUserId(User user) {
        return strategyRepository.findByUserId(user.getSeqId());
    }

    public List<Strategy> findStrategyByChatId(String chatId) {
        User user = userRepository.findByTelegramId(chatId);
        if (user != null) {
            return strategyRepository.findByUserId(user.getSeqId());
        } else {
            return new ArrayList<>();
        }

    }

    public List<Strategy> findStrategyActive() {
        List<Strategy> strategies = strategyRepository.findByStatus(StrategyStatus.ACTIVE.toString());
        if (strategies != null) {
            return strategies;
        } else {
            return new ArrayList<>();
        }
    }
}
