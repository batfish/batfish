package org.batfish.common;

public enum WellKnownCommunity {
  INTERNET(0L),
  GSHUT(0xFFFFFF04L),
  LOCAL_AS(0xFFFFFF03L),
  NO_ADVERTISE(0xFFFFFF02L),
  NO_EXPORT(0xFFFFFF01L);

  private final long _value;

  WellKnownCommunity(long value) {
    this._value = value;
  }

  public long getValue() {
    return this._value;
  }
}
