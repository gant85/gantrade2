package com.gant.trade.strategy;


import com.gant.trade.exception.ActionNotFoundException;
import com.gant.trade.exception.ConditionBadRequeestException;
import com.gant.trade.exception.ConditionNotFoundException;
import com.gant.trade.exception.IndicatorNotFoundException;
import com.gant.trade.rest.model.*;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TradeStrategy extends TradeStrategyFactory {

    private StrategyTO strategyTO;
    private ClosePriceIndicator closePriceIndicator;

    public TradeStrategy(BarSeries barSeries, StrategyTO strategyTO) {
        super(barSeries);
        this.strategyTO = strategyTO;
        this.closePriceIndicator = new ClosePriceIndicator(barSeries);
    }

    @Override
    public Strategy getStrategy() {
        Map<ActionTypeEnum, List<RuleTO>> map = strategyTO.getRules().stream().collect(Collectors.groupingBy(ruleTO -> ruleTO.getAction().getType()));

        RuleTO firstBuyingRuleTO = map.get(ActionTypeEnum.BUY).remove(0);
        if (firstBuyingRuleTO == null) {
            throw new ActionNotFoundException();
        }
        Rule buyingRule = getRule(firstBuyingRuleTO.getCondition());
        for (RuleTO ruleTO : map.get(ActionTypeEnum.BUY)) {
            Rule rule = getRule(ruleTO.getCondition());
            if (OperatorEnum.AND.equals(ruleTO.getOperator())) {
                buyingRule = buyingRule.and(rule);
            } else {
                buyingRule = buyingRule.or(rule);
            }
        }

        RuleTO firstSellingRuleTO = map.get(ActionTypeEnum.SELL).remove(0);
        if (firstSellingRuleTO == null) {
            throw new ActionNotFoundException();
        }
        Rule sellingRule = getRule(firstSellingRuleTO.getCondition());
        for (RuleTO ruleTO : map.get(ActionTypeEnum.SELL)) {
            Rule rule = getRule(ruleTO.getCondition());
            if (OperatorEnum.AND.equals(ruleTO.getOperator())) {
                sellingRule = sellingRule.and(rule);
            } else {
                sellingRule = sellingRule.or(rule);
            }
        }

        return new BaseStrategy(buyingRule, sellingRule);
    }

    @Override
    public String getName() {
        return this.strategyTO.getName();
    }

    @Override
    public double getContracts(double portfolioValue, int barIndex) {
        return 0;
    }

    private Rule getRule(ConditionTO conditionTO) {
        List<Indicator<Num>> indicators = getIndicators(conditionTO.getIndicators());
        Iterator<Indicator<Num>> indicatorIterator = indicators.iterator();
        Indicator<Num> indicator1 = indicatorIterator.hasNext() ? indicatorIterator.next() : null;
        Indicator<Num> indicator2 = indicatorIterator.hasNext() ? indicatorIterator.next() : null;
        BigDecimal threshold = conditionTO.getThreshold();

        switch (conditionTO.getType()) {
            case IS_EQUAL:
                if (indicator2 != null) {
                    return new IsEqualRule(indicator1, indicator2);
                } else if (threshold != null) {
                    return new IsEqualRule(indicator1, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case LOWER_THAN:
                if (indicator2 != null) {
                    return new UnderIndicatorRule(indicator1, indicator2);
                } else if (threshold != null) {
                    return new UnderIndicatorRule(indicator1, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case GREATER_THAN:
                if (indicator2 != null) {
                    return new OverIndicatorRule(indicator1, indicator2);
                } else if (threshold != null) {
                    return new OverIndicatorRule(indicator1, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case CROSSING_ABOVE:
                if (indicator2 != null) {
                    return new CrossedUpIndicatorRule(indicator1, indicator2);
                } else if (threshold != null) {
                    return new CrossedUpIndicatorRule(indicator1, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case CROSSING_BELOW:
                if (indicator2 != null) {
                    return new CrossedDownIndicatorRule(indicator1, indicator2);
                } else if (threshold != null) {
                    return new CrossedDownIndicatorRule(indicator1, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case STOP_GAIN:
                if (threshold != null) {
                    return new StopGainRule(closePriceIndicator, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            case STOP_LOSS:
                if (threshold != null) {
                    return new StopLossRule(closePriceIndicator, threshold);
                } else {
                    throw new ConditionBadRequeestException(new Object[]{conditionTO.getType()});
                }
            default:
                throw new ConditionNotFoundException();
        }
    }

    private List<Indicator<Num>> getIndicators(List<IndicatorTO> indicatorTOList) {
        return getIndicators(indicatorTOList, null);
    }

    private List<Indicator<Num>> getIndicators(List<IndicatorTO> indicatorTOList, IndicatorTypeEnum indicatorTypeRef) {
        Map<Boolean, List<IndicatorTO>> map = Optional.ofNullable(indicatorTOList).orElse(Collections.emptyList()).stream().collect(Collectors.partitioningBy(o -> o.getIndicatorTypeRef() == indicatorTypeRef));
        return map.get(true).stream().map(indicatorTO -> getIndicator(indicatorTO, map.get(false))).collect(Collectors.toList());
    }

    private Indicator<Num> getIndicator(IndicatorTO indicatorTO) {
        return getIndicator(indicatorTO, null);
    }

    private Indicator<Num> getIndicator(IndicatorTO indicatorTO, List<IndicatorTO> indicators) {
        switch (indicatorTO.getType()) {
            case EMA:
                return new EMAIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
            case SMA:
                return new SMAIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
            case RSI:
                return new RSIIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
            case ADX:
                return new ADXIndicator(barSeries, indicatorTO.getPeriod().intValue());
            case LOW_PRICE:
                return new LowPriceIndicator(barSeries);
            case HIGH_PRICE:
                return new HighPriceIndicator(barSeries);
            case CLOSE_PRICE:
                return closePriceIndicator;
            case BOLLINGER_BAND_MIDDLE: {
                if (CollectionUtils.isEmpty(indicators)) {
                    throw new IndicatorNotFoundException();
                }
                List<Indicator<Num>> indicatorList = getIndicators(indicators, IndicatorTypeEnum.BOLLINGER_BAND_MIDDLE);
                SMAIndicator smaIndicator = (SMAIndicator) indicatorList.get(0);
                return new BollingerBandsMiddleIndicator(smaIndicator);
            }
            case BOLLINGER_BAND_LOWER: {
                if (CollectionUtils.isEmpty(indicators)) {
                    throw new IndicatorNotFoundException();
                }
                List<Indicator<Num>> indicatorList = getIndicators(indicators, IndicatorTypeEnum.BOLLINGER_BAND_LOWER);
                SMAIndicator smaIndicator = (SMAIndicator) indicatorList.get(0);
                StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
                BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
                if (indicatorTO.getDeviation() != null) {
                    return new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, standardDeviation, barSeries.numOf(indicatorTO.getDeviation()));
                }
                return new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, standardDeviation);
            }
            case BOLLINGER_BAND_UPPER: {
                if (CollectionUtils.isEmpty(indicators)) {
                    throw new IndicatorNotFoundException();
                }
                List<Indicator<Num>> indicatorList = getIndicators(indicators, IndicatorTypeEnum.BOLLINGER_BAND_UPPER);
                SMAIndicator smaIndicator = (SMAIndicator) indicatorList.get(0);
                StandardDeviationIndicator standardDeviationIndicator = new StandardDeviationIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
                BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
                if (indicatorTO.getDeviation() != null) {
                    return new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator, barSeries.numOf(indicatorTO.getDeviation()));
                }
                return new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator);
            }
            case BOLLINGER_BAND_WIDTH: {
                if (CollectionUtils.isEmpty(indicators)) {
                    throw new IndicatorNotFoundException();
                }
                List<Indicator<Num>> indicatorList = getIndicators(indicators, IndicatorTypeEnum.BOLLINGER_BAND_WIDTH);
                SMAIndicator smaIndicator = (SMAIndicator) indicatorList.get(0);
                StandardDeviationIndicator standardDeviationIndicator = new StandardDeviationIndicator(closePriceIndicator, indicatorTO.getPeriod().intValue());
                BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
                BollingerBandsLowerIndicator bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator);
                BollingerBandsUpperIndicator bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator);
                return new BollingerBandWidthIndicator(bollingerBandsUpperIndicator, bollingerBandsMiddleIndicator, bollingerBandsLowerIndicator);
            }
            case STOCHASTIC_RSI:
                return new StochasticRSIIndicator(barSeries, indicatorTO.getPeriod().intValue());
            case STOCHASTIC_OSCILLATOR_D:
                List<Indicator<Num>> indicatorList = getIndicators(indicators, IndicatorTypeEnum.STOCHASTIC_OSCILLATOR_D);
                StochasticOscillatorKIndicator stochasticOscillatorKIndicator = (StochasticOscillatorKIndicator) indicatorList.get(0);
                return new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);
            case STOCHASTIC_OSCILLATOR_K:
                return new StochasticOscillatorKIndicator(barSeries, indicatorTO.getPeriod().intValue());
            default:
                throw new IndicatorNotFoundException();
        }
    }
}