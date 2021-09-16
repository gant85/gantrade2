package com.gant.trade.service.impl.binance;

import com.gant.binance.api.client.BinanceApiRestClient;
import com.gant.binance.api.client.domain.market.Candlestick;
import com.gant.binance.api.client.domain.market.CandlestickInterval;
import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.CandlestickSymbol;
import com.gant.trade.model.Timeframe;
import com.gant.trade.service.HistoricalCandlesService;
import com.gant.trade.utility.BarSeriesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class HistoricalCandlesBinanceService implements HistoricalCandlesService<BinanceApiRestClient,Candlestick> {

    @Override
    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(BinanceApiRestClient exchange, Timeframe timeframe, List<SymbolInfo> tradedCurrencies) {
        return requestHistoricalCandles(exchange, timeframe, tradedCurrencies, null, null);
    }

    @Override
    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(BinanceApiRestClient exchange, Timeframe timeframe, List<SymbolInfo> tradedCurrencies, Long startTime, Long endTime) {
        log.info("Request historical candles");

        Map<CandlestickSymbol, BarSeries> barSeries = new HashMap<>();

        for (SymbolInfo symbolInfo : tradedCurrencies) {

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
    public List<Candlestick> getCandlestickBars(BinanceApiRestClient exchange, Timeframe timeframe, SymbolInfo symbolInfo) {
        return getCandlestickBars(exchange, timeframe, symbolInfo, null, null);
    }

    @Override
    public List<Candlestick> getCandlestickBars(BinanceApiRestClient exchange, Timeframe timeframe, SymbolInfo symbolInfo, Long startTime, Long endTime) {
        if (startTime != null && endTime != null) {
            return exchange.getCandlestickBars(symbolInfo.getSymbol(), CandlestickInterval.valueOf(timeframe.name()), 500, startTime, endTime);
        } else {
            return exchange.getCandlestickBars(symbolInfo.getSymbol(), CandlestickInterval.valueOf(timeframe.name()));
        }
    }
}
