package com.gant.trade.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ConditionType
 */
public enum ConditionType {

  IS_EQUAL("IS_EQUAL"),

  LOWER_THAN("LOWER_THAN"),

  GREATER_THAN("GREATER_THAN"),

  CROSSING_ABOVE("CROSSING_ABOVE"),

  CROSSING_BELOW("CROSSING_BELOW"),

  STOP_GAIN("STOP_GAIN"),

  STOP_LOSS("STOP_LOSS"),

  STOP_LOSS_FROM_HIGHEST_PRICE("STOP_LOSS_FROM_HIGHEST_PRICE"),

  STOCHASTIC_RSI_BOLLINGER_BAND_EXIT_RULE("STOCHASTIC_RSI_BOLLINGER_BAND_EXIT_RULE");

  private String value;

  ConditionType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ConditionType fromValue(String value) {
    for (ConditionType b : ConditionType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

