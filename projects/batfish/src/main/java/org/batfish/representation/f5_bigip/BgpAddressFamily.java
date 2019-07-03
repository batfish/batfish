package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Top-level BGP-process-wide address-family configuration */
@ParametersAreNonnullByDefault
public abstract class BgpAddressFamily implements Serializable {

  private @Nonnull Map<F5BigipRoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;

  public BgpAddressFamily() {
    _redistributionPolicies = new HashMap<>();
  }

  public Map<F5BigipRoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }
}
