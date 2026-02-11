package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Global BFD settings for FortiOS */
public class BfdSettings implements Serializable {
  private @Nullable Integer _interval;
  private @Nullable Integer _minRx;
  private @Nullable Integer _minTx;
  private @Nullable Integer _multiplier;

  public static final int DEFAULT_INTERVAL = 50; // 50ms default
  public static final int DEFAULT_MIN_RX = 50;
  public static final int DEFAULT_MIN_TX = 50;
  public static final int DEFAULT_MULTIPLIER = 3;

  public @Nullable Integer getInterval() {
    return _interval;
  }

  public @Nonnull Integer getIntervalEffective() {
    return _interval != null ? _interval : DEFAULT_INTERVAL;
  }

  public @Nullable Integer getMinRx() {
    return _minRx;
  }

  public @Nonnull Integer getMinRxEffective() {
    return _minRx != null ? _minRx : DEFAULT_MIN_RX;
  }

  public @Nullable Integer getMinTx() {
    return _minTx;
  }

  public @Nonnull Integer getMinTxEffective() {
    return _minTx != null ? _minTx : DEFAULT_MIN_TX;
  }

  public @Nullable Integer getMultiplier() {
    return _multiplier;
  }

  public @Nonnull Integer getMultiplierEffective() {
    return _multiplier != null ? _multiplier : DEFAULT_MULTIPLIER;
  }

  public void setInterval(Integer interval) {
    _interval = interval;
  }

  public void setMinRx(Integer minRx) {
    _minRx = minRx;
  }

  public void setMinTx(Integer minTx) {
    _minTx = minTx;
  }

  public void setMultiplier(Integer multiplier) {
    _multiplier = multiplier;
  }
}
