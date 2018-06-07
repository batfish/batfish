package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class IsisLevelSettings implements Serializable {

  private static final String PROP_WIDE_METRICS_ONLY = "wideMetricsOnly";

  private static final long serialVersionUID = 1L;

  private boolean _wideMetricsOnly;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IsisLevelSettings)) {
      return false;
    }
    IsisLevelSettings rhs = (IsisLevelSettings) obj;
    return _wideMetricsOnly == rhs._wideMetricsOnly;
  }

  @JsonProperty(PROP_WIDE_METRICS_ONLY)
  public boolean getWideMetricsOnly() {
    return _wideMetricsOnly;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_wideMetricsOnly);
  }

  @JsonProperty(PROP_WIDE_METRICS_ONLY)
  public void setWideMetricsOnly(boolean wideMetricsOnly) {
    _wideMetricsOnly = wideMetricsOnly;
  }
}
