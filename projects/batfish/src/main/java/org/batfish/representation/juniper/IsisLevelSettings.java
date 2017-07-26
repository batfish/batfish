package org.batfish.representation.juniper;

import java.io.Serializable;

public class IsisLevelSettings implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _enabled;

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
