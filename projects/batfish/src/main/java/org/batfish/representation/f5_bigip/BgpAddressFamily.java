package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Top-level BGP-process-wide address-family configuration */
@ParametersAreNonnullByDefault
public abstract class BgpAddressFamily implements Serializable {

  private final @Nonnull Map<F5BigipRoutingProtocol, BgpRedistributionPolicy>
      _redistributionPolicies;

  public BgpAddressFamily() {
    _redistributionPolicies = new EnumMap<>(F5BigipRoutingProtocol.class);
  }

  public @Nonnull Map<F5BigipRoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }
}
