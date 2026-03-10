package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents the if-exceeding clause of a Juniper firewall policer. */
public final class PolicerIfExceeding implements Serializable {

  private @Nullable Long _bandwidthLimit;
  private @Nullable Long _burstSizeLimit;

  public @Nullable Long getBandwidthLimit() {
    return _bandwidthLimit;
  }

  public void setBandwidthLimit(@Nullable Long bandwidthLimit) {
    _bandwidthLimit = bandwidthLimit;
  }

  public @Nullable Long getBurstSizeLimit() {
    return _burstSizeLimit;
  }

  public void setBurstSizeLimit(@Nullable Long burstSizeLimit) {
    _burstSizeLimit = burstSizeLimit;
  }
}
