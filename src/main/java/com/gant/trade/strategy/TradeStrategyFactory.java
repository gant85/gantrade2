package com.gant.trade.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;

public abstract class TradeStrategyFactory {

    /**
     * The time series
     */
    protected final BarSeries barSeries;

    /**
     * The open price indicator
     */
    protected final OpenPriceIndicator openPriceIndicator;

    /**
     * The close price indicator
     */
    protected final ClosePriceIndicator closePriceIndicator;

    protected TradeStrategyFactory(final BarSeries barSeries) {
        this.barSeries = barSeries;
        this.closePriceIndicator = new ClosePriceIndicator(barSeries);
        this.openPriceIndicator = new OpenPriceIndicator(barSeries);
    }

    public BarSeries getBarSeries() {
        return barSeries;
    }

    /**
     * Get the strategy
     *
     * @return
     */
    public abstract Strategy getStrategy();

    /**
     * Get the name of the strategy
     *
     * @return
     */
    public abstract String getName();

    /**
     * Get the amount of contracts for the given portfolio value
     */
    public abstract double getContracts(final double portfolioValue, final int barIndex);
}

