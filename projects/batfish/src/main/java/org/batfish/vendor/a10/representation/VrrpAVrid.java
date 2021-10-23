package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Configuration for a vrrp-a vrid */
public final class VrrpAVrid implements Serializable {

  public VrrpAVrid() {
    _floatingIps = ImmutableSet.of();
  }

  public @Nullable VrrpaVridBladeParameters getBladeParameters() {
    return _bladeParameters;
  }

  public @Nonnull VrrpaVridBladeParameters getOrCreateBladeParameters() {
    if (_bladeParameters == null) {
      _bladeParameters = new VrrpaVridBladeParameters();
    }
    return _bladeParameters;
  }

  public @Nonnull Set<Ip> getFloatingIps() {
    return _floatingIps;
  }

  public void addFloatingIp(Ip floatingIp) {
    if (_floatingIps.contains(floatingIp)) {
      return;
    }
    _floatingIps =
        ImmutableSet.<Ip>builderWithExpectedSize(_floatingIps.size() + 1)
            .addAll(_floatingIps)
            .add(floatingIp)
            .build();
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
  private @Nonnull Set<Ip> _floatingIps;
  private boolean _preemptModeDisable;
  private @Nullable Integer _preemptModeThreshold;
}
