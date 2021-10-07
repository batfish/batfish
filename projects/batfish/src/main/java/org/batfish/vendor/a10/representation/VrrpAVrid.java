package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a vrrp-a vrid */
public final class VrrpAVrid implements Serializable {

  public @Nullable VrrpaVridBladeParameters getBladeParameters() {
    return _bladeParameters;
  }

  public @Nonnull VrrpaVridBladeParameters getOrCreateBladeParameters() {
    if (_bladeParameters == null) {
      _bladeParameters = new VrrpaVridBladeParameters();
    }
    return _bladeParameters;
  }

  public boolean getPreemptModeDisable() {
    return _preemptModeDisable;
  }

  public void setPreemptModeDisable(boolean preemptModeDisable) {
    _preemptModeDisable = preemptModeDisable;
  }

  public @Nullable Integer getPreemptModeThreshold() {
    return _preemptModeThreshold;
  }

  public void setPreemptModeThreshold(@Nullable Integer preemptModeThreshold) {
    _preemptModeThreshold = preemptModeThreshold;
  }

  private @Nullable VrrpaVridBladeParameters _bladeParameters;
  private boolean _preemptModeDisable;
  private @Nullable Integer _preemptModeThreshold;
}
