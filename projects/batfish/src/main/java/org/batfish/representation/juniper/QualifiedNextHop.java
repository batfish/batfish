package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a qualified next-hop configured for a {@link StaticRoute} */
public class QualifiedNextHop implements Serializable {
  private @Nullable Integer _bfdLivenessDetectionDetectionTimeThreshold;
  private @Nullable Integer _bfdLivenessDetectionHolddownInterval;
  private @Nullable Integer _bfdLivenessDetectionMinimumInterval;
  private @Nullable Integer _bfdLivenessDetectionMinimumReceiveInterval;
  private @Nullable Integer _bfdLivenessDetectionMultiplier;
  private @Nullable Boolean _bfdLivenessDetectionNoAdaptation;
  private @Nullable Integer _bfdLivenessDetectionTransmitIntervalMinimumInterval;
  private @Nullable Integer _bfdLivenessDetectionTransmitIntervalThreshold;
  private @Nullable Integer _metric;
  private @Nonnull NextHop _nextHop;
  private @Nullable Integer _preference;
  private @Nullable Long _tag;

  public QualifiedNextHop(@Nonnull NextHop nextHop) {
    _nextHop = nextHop;
  }

  public @Nullable Integer getBfdLivenessDetectionDetectionTimeThreshold() {
    return _bfdLivenessDetectionDetectionTimeThreshold;
  }

  public void setBfdLivenessDetectionDetectionTimeThreshold(int detectionTimeThreshold) {
    _bfdLivenessDetectionDetectionTimeThreshold = detectionTimeThreshold;
  }

  public @Nullable Integer getBfdLivenessDetectionHolddownInterval() {
    return _bfdLivenessDetectionHolddownInterval;
  }

  public void setBfdLivenessDetectionHolddownInterval(int holddownInterval) {
    _bfdLivenessDetectionHolddownInterval = holddownInterval;
  }

  public @Nullable Integer getBfdLivenessDetectionMinimumInterval() {
    return _bfdLivenessDetectionMinimumInterval;
  }

  public void setBfdLivenessDetectionMinimumInterval(int minimumInterval) {
    _bfdLivenessDetectionMinimumInterval = minimumInterval;
  }

  public @Nullable Integer getBfdLivenessDetectionMinimumReceiveInterval() {
    return _bfdLivenessDetectionMinimumReceiveInterval;
  }

  public void setBfdLivenessDetectionMinimumReceiveInterval(int minimumReceiveInterval) {
    _bfdLivenessDetectionMinimumReceiveInterval = minimumReceiveInterval;
  }

  public @Nullable Integer getBfdLivenessDetectionMultiplier() {
    return _bfdLivenessDetectionMultiplier;
  }

  public void setBfdLivenessDetectionMultiplier(int multiplier) {
    _bfdLivenessDetectionMultiplier = multiplier;
  }

  public @Nullable Boolean getBfdLivenessDetectionNoAdaptation() {
    return _bfdLivenessDetectionNoAdaptation;
  }

  public void setBfdLivenessDetectionNoAdaptation(boolean noAdaptation) {
    _bfdLivenessDetectionNoAdaptation = noAdaptation;
  }

  public @Nullable Integer getBfdLivenessDetectionTransmitIntervalMinimumInterval() {
    return _bfdLivenessDetectionTransmitIntervalMinimumInterval;
  }

  public void setBfdLivenessDetectionTransmitIntervalMinimumInterval(int minimumInterval) {
    _bfdLivenessDetectionTransmitIntervalMinimumInterval = minimumInterval;
  }

  public @Nullable Integer getBfdLivenessDetectionTransmitIntervalThreshold() {
    return _bfdLivenessDetectionTransmitIntervalThreshold;
  }

  public void setBfdLivenessDetectionTransmitIntervalThreshold(int threshold) {
    _bfdLivenessDetectionTransmitIntervalThreshold = threshold;
  }

  public @Nullable Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  public @Nullable Integer getPreference() {
    return _preference;
  }

  public void setPreference(@Nullable Integer preference) {
    _preference = preference;
  }

  public @Nullable Long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = tag;
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }
}
