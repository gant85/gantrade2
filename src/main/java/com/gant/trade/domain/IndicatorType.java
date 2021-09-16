package com.gant.trade.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets IndicatorType
 */
public enum IndicatorType {

  RSI("RSI"),

  EMA("EMA"),

  SMA("SMA"),

  ADX("ADX"),

  BOLLINGER_BAND_MIDDLE("BOLLINGER_BAND_MIDDLE"),

  BOLLINGER_BAND_LOWER("BOLLINGER_BAND_LOWER"),

  BOLLINGER_BAND_UPPER("BOLLINGER_BAND_UPPER"),

  BOLLINGER_BAND_WIDTH("BOLLINGER_BAND_WIDTH"),

  LOW_PRICE("LOW_PRICE"),

  HIGH_PRICE("HIGH_PRICE"),

  CLOSE_PRICE("CLOSE_PRICE"),

  DIFFERENCE_PERCENTAGE("DIFFERENCE_PERCENTAGE"),

  STOCHASTIC_RSI("STOCHASTIC_RSI"),

  STOCHASTIC_OSCILLATOR_D("STOCHASTIC_OSCILLATOR_D"),

  STOCHASTIC_OSCILLATOR_K("STOCHASTIC_OSCILLATOR_K");

  private String value;

  IndicatorType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static IndicatorType fromValue(String value) {
    for (IndicatorType b : IndicatorType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

