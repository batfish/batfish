package org.batfish.representation.juniper;

import java.io.Serializable;

public class IsisLevelSettings implements Serializable {

  // Enabled by default
  private boolean _enabled = true;
  private boolean _wideMetricsOnly;

  public boolean getEnabled() {
    return _enabled;
  }

  public boolean getWideMetricsOnly() {
    return _wideMetricsOnly;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setWideMetricsOnly(boolean wideMetricsOnly) {
    _wideMetricsOnly = wideMetricsOnly;
  }
}
