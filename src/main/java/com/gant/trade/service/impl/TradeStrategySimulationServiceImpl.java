package com.gant.trade.service.impl;

import com.gant.trade.domain.Trade;
import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.exception.StrategyNotFoundException;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.Timeframe;
import com.gant.trade.model.TradeDirection;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.rest.model.*;
import com.gant.trade.service.TradeStrategySimulationService;
import com.gant.trade.utility.BarMerger;
import com.gant.trade.utility.BarSeriesUtil;
import com.gant.trade.utility.DecimalFormatUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.Bar;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class TradeStrategySimulationServiceImpl implements TradeStrategySimulationService {

    private double fee = 0D;
    private List<StrategySimulation> strategySimulations;
    private Map<String, TradingRecord> tradingRecordMap;
    private Map<String, BarSeries> barSeries;
    private Trade openTrade;
    private StrategyTO strategyTO;
    private Timeframe timeframe;
    private List<SymbolInfoTO> tradedCurrencies;
    private final StrategyService strategyService;
    private final UserRepository userRepository;
    private Map<String, Strategy> strategies;
    private final ApplicationContext applicationContext;

    public TradeStrategySimulationServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.strategyService = applicationContext.getBean(StrategyService.class);
        this.userRepository = applicationContext.getBean(UserRepository.class);
    }

    @Override
    public StrategySimulationResponse simulation(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException {
        exchangeConfiguration(strategySimulationRequest.getUserId());

        if (strategySimulationRequest.getFee() != null) {
            fee = strategySimulationRequest.getFee();
        }

        StrategySimulationResponse strategySimulationResponse = new StrategySimulationResponse();
        strategySimulations = new ArrayList<>();

        for (Long strategyId : Optional.ofNullable(strategySimulationRequest.getStrategyId()).orElse(Collections.emptyList())) {
            tradingRecordMap = new HashMap<>();
            barSeries = new HashMap<>();
            openTrade = null;
            strategyTO = this.strategyService.getStrategyById(strategyId);
            timeframe = Timeframe.getTimeframe(this.strategyTO.getTimeframe().toString());
            if (timeframe == null) {
                throw new StrategyNotFoundException();
            }
            tradedCurrencies = getCurrencies();
            requestHistoricalCandles(strategySimulationRequest);
            strategies = TradeStrategyServiceUtil.getStrategies(tradedCurrencies, barSeries, strategyTO);
            Map<SymbolInfoTO, BarMerger> candleMerger = tradedCurrencies.stream().collect(Collectors.toMap(currencyPair1 -> currencyPair1, currencyPair2 -> new BarMerger(currencyPair2, timeframe, strategyTO.getCheckRulesEveryTime(), strategyTO.getCheckRulesEveryTimeValue(), this::barDoneCallback)));

            for (SymbolInfoTO symbolInfo : tradedCurrencies) {
                List<Candlestick> candlesticks = getCandlesticks(symbolInfo, strategySimulationRequest.getStartDate(), strategySimulationRequest.getEndDate());
                for (Candlestick candlestick : candlesticks) {
                    LocalDateTime closeTime = Instant.ofEpochMilli(candlestick.getCloseTime()).atZone(ZoneId.systemDefault()).toLocalDateTime().plusSeconds(1);
                    boolean isBarFinal = closeTime.getMinute() % timeframe.getValue() == 0;
                    candleMerger.get(symbolInfo).addNewPrice(closeTime.minusSeconds(10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), new BigDecimal(candlestick.getOpen()), new BigDecimal(candlestick.getLow()), new BigDecimal(candlestick.getVolume()), false);
                    candleMerger.get(symbolInfo).addNewPrice(closeTime.minusSeconds(5).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), new BigDecimal(candlestick.getLow()), new BigDecimal(candlestick.getHigh()), new BigDecimal(candlestick.getVolume()), false);
                    candleMerger.get(symbolInfo).addNewPrice(candlestick.getCloseTime(), new BigDecimal(candlestick.getHigh()), new BigDecimal(candlestick.getClose()), new BigDecimal(candlestick.getVolume()), isBarFinal);
                }
            }
            strategySimulationResponse.setStrategySimulations(strategySimulations);
        }
        double gain = strategySimulations.stream().mapToDouble(StrategySimulation::getGain).sum();
        strategySimulationResponse.setGain(gain);
        double lost = strategySimulations.stream().mapToDouble(StrategySimulation::getGain).filter(v -> v < 0).sum();
        strategySimulationResponse.setLost(lost);
        strategySimulationResponse.setFee((strategySimulations.size() * 2) * fee);

        return strategySimulationResponse;
    }

    @Override
    public void exchangeConfiguration(Long userId) throws BusinessRuntimeException {
        // Override this in the exchange implementation
    }

    @Override
    public List<SymbolInfoTO> getCurrencies() {
        // Override this in the exchange implementation
        return null;
    }

    @Override
    public void requestHistoricalCandles(StrategySimulationRequest strategySimulationRequest) throws BusinessRuntimeException {
        // Override this in the exchange implementation
    }

    @Override
    public List<Candlestick> getCandlesticks(SymbolInfoTO symbolInfo, LocalDate startDate, LocalDate endDate) throws BusinessRuntimeException {
        List<Candlestick> candlesticks = new ArrayList<>();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        while (start.compareTo(end) <= 0) {
            long startTime = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = start.plusHours(12).minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            candlesticks.addAll(getCandlesticksExchange(Timeframe.ONE_MINUTE, symbolInfo, startTime, endTime));
            startTime = start.plusHours(12).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            endTime = start.plusDays(1).minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            candlesticks.addAll(getCandlesticksExchange(Timeframe.ONE_MINUTE, symbolInfo, startTime, endTime));
            start = start.plusDays(1);
        }
        return candlesticks;
    }

    @Override
    public List<Candlestick> getCandlesticksExchange(Timeframe timeframe, SymbolInfoTO symbolInfo, Long startTime, Long endTime) throws BusinessRuntimeException {
        // Override this in the exchange implementation
        return null;
    }

    @Override
    public void barDoneCallback(final SymbolInfoTO symbolInfo, final Bar bar) {
        try {
            BarSeriesUtil.addBar(barSeries, symbolInfo, bar, strategyTO.getName());
            shouldEnterExit(symbolInfo, bar);
        } catch (Exception e) {
            log.error("{} Got an exception while executing order {}", strategyTO.getName(), e);
        }
    }

    @Override
    public void shouldEnterExit(final SymbolInfoTO symbolInfo, final Bar bar) {
        BarSeries bs = barSeries.get(symbolInfo.getSymbol());
        final int endIndex = bs.getEndIndex();
        if (!tradingRecordMap.containsKey(symbolInfo.getSymbol())) {
            tradingRecordMap.put(symbolInfo.getSymbol(), new BaseTradingRecord());
        }
        TradingRecord tradingRecord = tradingRecordMap.get(symbolInfo.getSymbol());
        log.debug("{} symbolInfo={} price={} openTrade={}", strategyTO.getName(), symbolInfo.getSymbol(), bar.getClosePrice(), openTrade != null);

        if (openTrade == null && strategies.get(symbolInfo.getSymbol()).shouldEnter(endIndex)) {
            openTrade = new Trade(strategyTO.getSeqId(), strategyTO.getUserId(), strategyTO.getExchange(), TradeDirection.LONG.name(), symbolInfo.getSymbol(), Double.parseDouble(symbolInfo.getOrderSize()));
            openTrade.setExpectedPriceOpen(bar.getClosePrice().doubleValue());
            double amount = getAmount(bar.getClosePrice().doubleValue(), symbolInfo);
            openTrade.setAmount(amount);
            openTrade.setTradeState(TradeState.OPEN);
            openTrade.setInsertionTime(Date.from(bar.getEndTime().toInstant()));
            buildStrategySimulation(bar);
            boolean enter = tradingRecord.enter(endIndex, bar.getClosePrice(), DecimalNum.valueOf(amount));
            log.info("Order enter = {}", enter);
        } else {
            log.info("Currency Order is Open={}", tradingRecord.getCurrentPosition().isOpened());
            if (openTrade != null && strategies.get(symbolInfo.getSymbol()).shouldExit(endIndex, tradingRecord)) {
                openTrade.setExpectedPriceClose(bar.getClosePrice().doubleValue());
                openTrade.setTradeState(TradeState.CLOSED);
                double gain = ((openTrade.getExpectedPriceClose() - openTrade.getExpectedPriceOpen()) * openTrade.getAmount()) - (fee * 2);
                openTrade.setGain(gain);
                buildStrategySimulation(bar);
                openTrade = null;
                boolean exit = tradingRecord.exit(endIndex, bar.getClosePrice(), DecimalNum.valueOf(gain));
                log.info("Order exit = {}", exit);
            }
        }
    }

    public Double getAmount(Double price, SymbolInfoTO symbolInfo) {
        // Override this in the exchange implementation
        return null;
    }

    @Override
    public void buildStrategySimulation(Bar bar) {
        StrategySimulation strategySimulation = strategySimulations.stream().filter(s -> strategyTO.getSeqId().equals(s.getStrategyId()) && s.getExitPrice() < 0).findFirst().orElse(null);
        if (strategySimulation == null) {
            strategySimulation = new StrategySimulation();
            strategySimulations.add(strategySimulation);
        }

        strategySimulation.setStrategyId(strategyTO.getSeqId());
        strategySimulation.setEntryDate(openTrade.getInsertionTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        strategySimulation.setEntryAmount(openTrade.getAmount());
        strategySimulation.setEntryPrice(openTrade.getExpectedPriceOpen());
        strategySimulation.setExitDate(bar.getEndTime().toLocalDateTime());
        strategySimulation.setExitPrice(openTrade.getExpectedPriceClose());
        if (strategySimulation.getExitPrice() > 0.0) {
            String gain = DecimalFormatUtil.format(openTrade.getGain());
            strategySimulation.setGain(Double.parseDouble(gain));
        } else {
            strategySimulation.setGain(0D);
        }
    }
}
