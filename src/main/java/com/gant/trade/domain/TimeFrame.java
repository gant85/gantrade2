package com.gant.trade.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets TimeFrame
 */
public enum TimeFrame {
  
  _1M("1m"),
  
  _3M("3m"),
  
  _5M("5m"),
  
  _15M("15m"),
  
  _30M("30m"),
  
  _1H("1h"),
  
  _2H("2h"),
  
  _4H("4h"),
  
  _6H("6h"),
  
  _8H("8h"),
  
  _12H("12h"),
  
  _1D("1d"),
  
  _3D("3d"),
  
  _1W("1w");

  private String value;

  TimeFrame(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static TimeFrame fromValue(String value) {
    for (TimeFrame b : TimeFrame.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

