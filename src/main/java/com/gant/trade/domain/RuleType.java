package com.gant.trade.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets RuleType
 */
public enum RuleType {
  
  CONDITION("CONDITION"),
  
  ACTION("ACTION"),
  
  OPERATOR("OPERATOR");

  private String value;

  RuleType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static RuleType fromValue(String value) {
    for (RuleType b : RuleType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

