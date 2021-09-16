package com.gant.trade.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ActionTypeEnum
 */
public enum ActionTypeEnum {
  
  BUY("BUY"),
  
  SELL("SELL");

  private String value;

  ActionTypeEnum(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ActionTypeEnum fromValue(String value) {
    for (ActionTypeEnum b : ActionTypeEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

