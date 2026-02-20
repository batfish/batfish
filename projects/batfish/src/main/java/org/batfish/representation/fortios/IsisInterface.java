package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing IS-IS interface configuration */
public final class IsisInterface implements Serializable {
  public static final IsisProcess.Level DEFAULT_CIRCUIT_TYPE = IsisProcess.Level.LEVEL_1_2;
  public static final int DEFAULT_METRIC = 10;
  public static final boolean DEFAULT_BFD = false;
  public static final boolean DEFAULT_STATUS = true;

  public IsisInterface(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable IsisProcess.Level getCircuitType() {
    return _circuitType;
  }

  public @Nonnull IsisProcess.Level getCircuitTypeEffective() {
    return firstNonNull(_circuitType, DEFAULT_CIRCUIT_TYPE);
  }

  public @Nullable Integer getMetric() {
    return _metric;
  }

  public int getMetricEffective() {
    return firstNonNull(_metric, DEFAULT_METRIC);
  }

  public @Nullable Integer getMetricLevel1() {
    return _metricLevel1;
  }

  public @Nullable Integer getMetricLevel2() {
    return _metricLevel2;
  }

  public @Nullable Boolean getBfd() {
    return _bfd;
  }

  public boolean getBfdEffective() {
    return firstNonNull(_bfd, DEFAULT_BFD);
  }

  public @Nullable Boolean getStatus() {
    return _status;
  }

  public boolean getStatusEffective() {
    return firstNonNull(_status, DEFAULT_STATUS);
  }

  public void setCircuitType(IsisProcess.Level circuitType) {
    _circuitType = circuitType;
  }

  public void setMetric(Integer metric) {
    _metric = metric;
  }

  public void setMetricLevel1(Integer metricLevel1) {
    _metricLevel1 = metricLevel1;
  }

  public void setMetricLevel2(Integer metricLevel2) {
    _metricLevel2 = metricLevel2;
  }

  public void setBfd(Boolean bfd) {
    _bfd = bfd;
  }

  public void setStatus(Boolean status) {
    _status = status;
  }

  private final @Nonnull String _name;
  private @Nullable IsisProcess.Level _circuitType;
  private @Nullable Integer _metric;
  private @Nullable Integer _metricLevel1;
  private @Nullable Integer _metricLevel2;
  private @Nullable Boolean _bfd;
  private @Nullable Boolean _status;
}
