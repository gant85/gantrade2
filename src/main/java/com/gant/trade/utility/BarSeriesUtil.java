package com.gant.trade.utility;

import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.Candlestick;
import com.gant.trade.model.Timeframe;
import com.gant.trade.rest.model.SymbolInfoTO;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
public class BarSeriesUtil {

    private BarSeriesUtil() {
    }

    public static void addBar(Map<String, BarSeries> barSeries, SymbolInfoTO symbolInfo, Bar bar, String strategyName) {
        BarSeries ts = barSeries.get(symbolInfo.getSymbol());
        if (ts.getLastBar().getBeginTime().isEqual(bar.getBeginTime())) {
            log.debug("{} {} {}: lastBarReplaced={}", strategyName, symbolInfo.getSymbol(), ts.getEndIndex(), bar);
            ts.addBar(bar, true);
        } else {
            log.debug("{} {} {}: lastBarAdded={}", strategyName, symbolInfo.getSymbol(), ts.getEndIndex(), bar);
            ts.addBar(bar);
        }
    }

    public static Bar convertCandlestick(final Candlestick candlestick, final Timeframe timeframe) {
        final Instant instant = Instant.ofEpochMilli(candlestick.getCloseTime());
        final ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        return new BaseBar(
                Duration.ofMillis(timeframe.getMilliSeconds()),
                time,
                candlestick.getOpen(),
                candlestick.getHigh(),
                candlestick.getLow(),
                candlestick.getClose(),
                candlestick.getVolume()
        );
    }

    public static com.gant.trade.domain.Bar convert(final Bar bar) {
        if (bar == null) {
            return null;
        }
        com.gant.trade.domain.Bar b = new com.gant.trade.domain.Bar();
        b.setAmount(bar.getAmount().doubleValue());
        b.setBeginTime(Date.from(bar.getBeginTime().toInstant()));
        b.setClosePrice(bar.getClosePrice().doubleValue());
        b.setEndTime(Date.from(bar.getEndTime().toInstant()));
        b.setMaxPrice(bar.getHighPrice().doubleValue());
        b.setMinPrice(bar.getLowPrice().doubleValue());
        b.setOpenPrice(bar.getOpenPrice().doubleValue());
        b.setTimePeriod(bar.getTimePeriod().toMinutes());
        b.setTrades(bar.getTrades());
        b.setVolume(bar.getVolume().doubleValue());
        return b;
    }
}
