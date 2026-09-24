package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

public class IsisLevelSettings implements Serializable {

  // Enabled by default
  private boolean _enabled = true;
  private @Nullable IsisFloodReflector _floodReflector;
  private boolean _wideMetricsOnly;

  public boolean getEnabled() {
    return _enabled;
  }

  public @Nullable IsisFloodReflector getFloodReflector() {
    return _floodReflector;
  }

  public boolean getWideMetricsOnly() {
    return _wideMetricsOnly;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setFloodReflector(IsisFloodReflector floodReflector) {
    _floodReflector = floodReflector;
  }

  public void setWideMetricsOnly(boolean wideMetricsOnly) {
    _wideMetricsOnly = wideMetricsOnly;
  }
}
