package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** IPv4 unicast BGP configuration for a VRF. */
public class BgpIpv4UnicastAddressFamily implements Serializable {

  private final @Nonnull Map<Prefix, BgpNetwork> _networks;
  private final @Nonnull Map<CumulusRoutingProtocol, BgpRedistributionPolicy>
      _redistributionPolicies;
  private final Map<String, BgpVrfNeighborAddressFamilyConfiguration>
      _neighborAddressFamilyConfigurations;

  public BgpIpv4UnicastAddressFamily() {
    _networks = new HashMap<>();
    _redistributionPolicies = new TreeMap<>();
    _neighborAddressFamilyConfigurations = new TreeMap<>();
  }

  public @Nonnull Map<Prefix, BgpNetwork> getNetworks() {
    return _networks;
  }

  public @Nonnull Map<CumulusRoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public @Nonnull Map<String, BgpVrfNeighborAddressFamilyConfiguration>
      getNeighborAddressFamilyConfigurations() {
    return _neighborAddressFamilyConfigurations;
  }
}
