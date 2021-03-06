package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiClientFactory;
import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.domain.User;
import com.gant.trade.model.Timeframe;
import com.gant.trade.mongo.repository.UserRepository;
import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.mongo.service.TradeService;
import com.gant.trade.rest.model.*;
import com.gant.trade.utility.SymbolInfoUtil;
import com.gant.trade.utility.TradeStrategyServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatusInfoService {

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private HistoricalCandlesBinanceService historicalCandlesBinanceService;

    @Autowired
    private SymbolInfoUtil symbolInfoUtil;

    public List<StrategyStatusInfoTO> getStrategyStatusInfoToList(Long seqId) {
        StrategyTO strategyTO = strategyService.getStrategyById(seqId);
        User user = userRepository.findBySeqId(strategyTO.getUserId());
        ExchangeConfiguration userExchange = TradeStrategyServiceUtil.getExchangeConfigurationByExchange(user, Exchange.BINANCE);
        if (userExchange == null) {
            return Collections.emptyList();
        }
        final BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance(userExchange.getApiKey(), userExchange.getSecretKey());
        BinanceApiRestClient binanceApiRestClient = binanceApiClientFactory.newRestClient();
        Timeframe timeframe = Timeframe.getTimeframe(strategyTO.getTimeframe().toString());
        List<SymbolInfo> currencies = strategyTO.getSymbolInfo().stream()
                .map(currency -> symbolInfoUtil.getSymbolInfoByExchange(strategyTO.getExchange(), strategyTO.getUserId(), currency.getSymbol(), currency.getOrderSize().doubleValue()))
                .collect(Collectors.toList());
        Map<String, BarSeries> barSeries = new HashMap<>();
        historicalCandlesBinanceService.requestHistoricalCandles(binanceApiRestClient, timeframe, currencies).forEach((k, v) -> barSeries.put(k.getSymbolInfo(), v));

        return getStrategyStatusInfoToList(strategyTO, barSeries, binanceApiRestClient);
    }

    List<StrategyStatusInfoTO> getStrategyStatusInfoToList(StrategyTO strategyTO, Map<String, BarSeries> barSeries, BinanceApiRestClient binanceApiRestClient) {
        return strategyTO.getSymbolInfo().stream().map(symbolInfo -> {
            StrategyStatusInfoTO strategyStatusInfoTO = new StrategyStatusInfoTO();
            String price = binanceApiRestClient.getPrice(symbolInfo.getSymbol()).getPrice();
            strategyStatusInfoTO.setPrice(Double.parseDouble(price));
            strategyStatusInfoTO.setRsi(new ArrayList<>());
            BarSeries bs = barSeries.get(symbolInfo.getSymbol());
            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(bs);
            List<IndicatorTO> rsiIndicators = strategyTO.getRules().stream()
                    .map(ruleTO -> ruleTO.getCondition().getIndicators().stream()
                            .filter(indicatorTO -> IndicatorTypeEnum.RSI.equals(indicatorTO.getType()) && indicatorTO.getIndicatorTypeRef() == null)
                            .collect(Collectors.toList())).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            for (IndicatorTO indicatorTO : rsiIndicators) {
                RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
                RSITO rsiTO = new RSITO();
                rsiTO.setPeriod(indicatorTO.getPeriod().intValue());
                rsiTO.setValue(rsiIndicator.getValue(bs.getEndIndex()).doubleValue());
                strategyStatusInfoTO.getRsi().add(rsiTO);
            }
            if (strategyStatusInfoTO.getRsi().isEmpty()) {
                RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
                RSITO rsiTO = new RSITO();
                rsiTO.setPeriod(14);
                rsiTO.setValue(rsiIndicator.getValue(bs.getEndIndex()).doubleValue());
                strategyStatusInfoTO.getRsi().add(rsiTO);
            }
            VolumeIndicator volumeIndicator = new VolumeIndicator(bs, 20); // TODO add volume to IndicatorTypeEnum
            strategyStatusInfoTO.setVolume(volumeIndicator.getValue(bs.getEndIndex()).doubleValue());

            strategyStatusInfoTO.setOrders(tradeService.getOpenOrderByCurrencyAndStrategySeqId(symbolInfo.getSymbol(), strategyTO.getSeqId()));

            return strategyStatusInfoTO;
        }).collect(Collectors.toList());
    }
}
