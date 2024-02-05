package com.gant.trade.service.impl.bybit;

import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.CandlestickSymbol;
import com.gant.trade.model.Timeframe;
import com.gant.trade.proxy.bybit.v5.ByBitProxy;
import com.gant.trade.proxy.bytbit.v5.model.GetKlineResponse;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.service.HistoricalCandlesService;
import com.gant.trade.utility.BarSeriesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class HistoricalCandlesByBitService implements HistoricalCandlesService<ByBitProxy, Candlestick> {
    @Override
    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(ByBitProxy exchange, Timeframe timeframe, List<SymbolInfoTO> tradedCurrencies) throws BusinessRuntimeException {
        return requestHistoricalCandles(exchange, timeframe, tradedCurrencies, null, null);
    }

    @Override
    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(ByBitProxy exchange, Timeframe timeframe, List<SymbolInfoTO> tradedCurrencies, Long startTime, Long endTime) throws BusinessRuntimeException {
        log.info("Request historical candles");

        Map<CandlestickSymbol, BarSeries> barSeries = new HashMap<>();

        for (SymbolInfoTO symbolInfo : tradedCurrencies) {

            String symbol = symbolInfo.getSymbol();
            BaseBarSeries currencyBarSeries = new BaseBarSeries(symbol);
            CandlestickSymbol barSymbol = new CandlestickSymbol(symbol, timeframe);

            barSeries.put(barSymbol, currencyBarSeries);

            CountDownLatch tickCountdown = new CountDownLatch(100);

            List<Candlestick> candlestickList = getCandlestickBars(exchange, timeframe, symbolInfo, startTime, endTime);
            if (!candlestickList.isEmpty()) {
                candlestickList.remove(candlestickList.size() - 1);
            }
            candlestickList.forEach(candlestick -> {
                BarSeries barSeriesToAdd = barSeries.get(barSymbol);
                Bar bar = BarSeriesUtil.convertCandlestick(candlestick, timeframe);
                try {
                    barSeriesToAdd.addBar(bar);
                    tickCountdown.countDown();
                } catch (IllegalArgumentException e) {
                    log.error("Unable to add tick {} to time series, last tick is {}", bar, barSeriesToAdd.getLastBar());
                }
            });
            log.info("Loaded {} candles for symbol {}", +barSeries.get(barSymbol).getEndIndex(), symbol);
        }

        return barSeries;
    }

    @Override
    public List<Candlestick> getCandlestickBars(ByBitProxy exchange, Timeframe timeframe, SymbolInfoTO symbolInfo) throws BusinessRuntimeException {
        return getCandlestickBars(exchange, timeframe, symbolInfo, null, null);
    }

    @Override
    public List<Candlestick> getCandlestickBars(ByBitProxy exchange, Timeframe timeframe, SymbolInfoTO symbolInfo, Long startTime, Long endTime) throws BusinessRuntimeException {
        List<Candlestick> list = new ArrayList<>();
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        Long currentStartTime = startTime;
        long currentEndTime = startTime + (timeframe.getMilliSeconds() * 180);
        while (currentEndTime < endTime) {
            GetKlineResponse getKlineResponse = exchange.getByBitMarketProxy().getKline(symbolInfo.getSymbol(), String.valueOf(timeframe.getValue()), "linear", currentStartTime, currentEndTime, null);
            if (getKlineResponse.getResult() != null && getKlineResponse.getResult().getList() != null) {
                for (List<String> l : getKlineResponse.getResult().getList()) {
                    long startTimeMillis = Long.parseLong(l.get(0));
                    Long endTimeMillis = startTimeMillis + timeframe.getMilliSeconds() - 1;
                    String openPrice = l.get(1);
                    String highPrice = l.get(2);
                    String lowPrice = l.get(3);
                    String closePrice = l.get(4);
                    String volume = l.get(5);
                    String turnover = l.get(6);
                    Candlestick candlestick = new Candlestick();
                    candlestick.setOpenTime(startTimeMillis);
                    candlestick.setCloseTime(endTimeMillis);
                    candlestick.setOpen(openPrice);
                    candlestick.setHigh(highPrice);
                    candlestick.setLow(lowPrice);
                    candlestick.setClose(closePrice);
                    candlestick.setVolume(volume);
                    list.add(candlestick);
                }
            }
            currentStartTime = currentEndTime;
            currentEndTime = currentEndTime + (timeframe.getMilliSeconds() * 180);
        }
        list.sort(Comparator.comparingLong(Candlestick::getOpenTime));
        return list;
    }
}
