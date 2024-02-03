package com.gant.trade.utility;


import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.Timeframe;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.io.Closeable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class BarMerger implements Closeable {

    private final Timeframe timeframe;
    private final BiConsumer<SymbolInfo, Bar> candleConsumer;
    private long timeframeBegin = -1;
    private double totalVolume = 0;
    private final List<BigDecimal> prices = new ArrayList<>();
    private final SymbolInfo symbolInfo;
    private long lastTimestamp = -1;
    private boolean checkRulesEveryTime = false;
    private int checkRulesEveryTimeValue = 0;

    public BarMerger(SymbolInfo symbolInfo, Timeframe timeframe, BiConsumer<SymbolInfo, Bar> candleConsumer) {
        this.symbolInfo = symbolInfo;
        this.timeframe = timeframe;
        this.candleConsumer = candleConsumer;
    }

    public BarMerger(SymbolInfo symbolInfo, Timeframe timeframe, boolean checkRulesEveryTime, int checkRulesEveryTimeValue, BiConsumer<SymbolInfo, Bar> candleConsumer) {
        this.symbolInfo = symbolInfo;
        this.timeframe = timeframe;
        this.checkRulesEveryTime = checkRulesEveryTime;
        this.checkRulesEveryTimeValue = checkRulesEveryTimeValue;
        this.candleConsumer = candleConsumer;
    }

    public void addNewPrice(long timestamp, BigDecimal priceOpen, BigDecimal priceClose, BigDecimal volume, boolean isBarFinal) {
        if (timeframeBegin == -1) {
            // Align timeFrame
            long millisecondsFromEpoch = timestamp / timeframe.getMilliSeconds();
            timeframeBegin = millisecondsFromEpoch * timeframe.getMilliSeconds();
        }
        if (lastTimestamp == -1) {
            lastTimestamp = System.currentTimeMillis();
        }
        if (prices.isEmpty()) {
            prices.add(priceOpen);
        } else {
            prices.add(priceClose);
        }

        totalVolume = totalVolume + volume.doubleValue();

        if (isBarFinal) {

            closeBar();

            while (timestamp >= timeframeBegin + timeframe.getMilliSeconds()) {
                timeframeBegin = timeframeBegin + timeframe.getMilliSeconds();
            }
        } else if (checkRulesEveryTime) {
            if (checkRulesEveryTimeValue == 0) {
                candleConsumerAccept();
            } else {
                checkCandleConsumer(timestamp);
            }
        }
    }

    private void checkCandleConsumer(long timestamp) {
        ZonedDateTime secondLastDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastTimestamp), ZoneId.systemDefault());
        ZonedDateTime lastDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        if (Duration.between(secondLastDate, lastDate).toMinutes() > checkRulesEveryTimeValue) {
            candleConsumerAccept();
            lastTimestamp = timestamp;
        }
    }

    public Bar getBar() {
        BigDecimal open = prices.get(0);
        BigDecimal close = prices.get(prices.size() - 1);
        double high = prices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(-1);
        double low = prices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(-1);
        Instant i = Instant.ofEpochMilli(timeframeBegin + timeframe.getMilliSeconds() - 1);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
        return new BaseBar(
                Duration.ofMillis(timeframe.getMilliSeconds()),
                endTime,
                String.valueOf(open),
                String.valueOf(high),
                String.valueOf(low),
                String.valueOf(close),
                String.valueOf(totalVolume)
        );
    }

    protected void closeBar() {
        if (prices.isEmpty()) {
            return;
        }
        candleConsumerAccept();
        totalVolume = 0;
        prices.clear();
    }

    private void candleConsumerAccept() {
        try {
            Bar bar = getBar();
            candleConsumer.accept(symbolInfo, bar);
        } catch (IllegalArgumentException e) {
            // ignore time shift
        }
    }

    @Override
    public void close() {
        closeBar();
    }

}
