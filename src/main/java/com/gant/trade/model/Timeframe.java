package com.gant.trade.model;

import java.util.concurrent.TimeUnit;

public enum Timeframe {
    ONE_MINUTE(1L, TimeUnit.MINUTES.toMillis(1L), "1m"),
    THREE_MINUTES(3L, TimeUnit.MINUTES.toMillis(3L), "3m"),
    FIVE_MINUTES(5L, TimeUnit.MINUTES.toMillis(5L), "5m"),
    FIFTEEN_MINUTES(15L, TimeUnit.MINUTES.toMillis(15L), "15m"),
    HALF_HOURLY(30L, TimeUnit.MINUTES.toMillis(30L), "30m"),
    HOURLY(1L, TimeUnit.HOURS.toMillis(1L), "1h"),
    TWO_HOURLY(2L, TimeUnit.HOURS.toMillis(2L), "2h"),
    FOUR_HOURLY(4L, TimeUnit.HOURS.toMillis(4L), "4h"),
    SIX_HOURLY(6L, TimeUnit.HOURS.toMillis(6L), "6h"),
    EIGHT_HOURLY(8L, TimeUnit.HOURS.toMillis(8L), "8h"),
    TWELVE_HOURLY(12L, TimeUnit.HOURS.toMillis(12L), "12h"),
    DAILY(1L, TimeUnit.DAYS.toMillis(1L), "1d"),
    THREE_DAILY(3L, TimeUnit.DAYS.toMillis(3L), "3d"),
    WEEKLY(7L, TimeUnit.DAYS.toMillis(7L), "1w"),
    MONTHLY(30L, TimeUnit.DAYS.toMillis(30L), "1M");

    private final long value;
    private final long milliseconds;
    private final String binanceString;

    Timeframe(long value, long milliseconds, String binanceString) {
        this.value = value;
        this.milliseconds = milliseconds;
        this.binanceString = binanceString;
    }

    public long getMilliSeconds() {
        return this.milliseconds;
    }

    public String getBinanceString() {
        return this.binanceString;
    }

    public long getValue() {
        return value;
    }

    public Long getMinutes(){ return TimeUnit.MILLISECONDS.toMinutes(this.milliseconds);}

    public static Timeframe getTimeframe(String binanceString) {
        Timeframe[] timeframes = values();
        for (Timeframe timeframe : timeframes) {
            if (timeframe.getBinanceString().equals(binanceString)) {
                return timeframe;
            }
        }
        return null;
    }
}

