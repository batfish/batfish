package org.batfish.representation.frr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** IPv4 unicast BGP configuration for a VRF. */
public class BgpIpv4UnicastAddressFamily implements Serializable {

  private final @Nonnull Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration>
      _aggregateNetworks;
  private final @Nonnull Map<Prefix, BgpNetwork> _networks;
  private final @Nonnull Map<FrrRoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;

  public BgpIpv4UnicastAddressFamily() {
    _aggregateNetworks = new HashMap<>();
    _networks = new HashMap<>();
    _redistributionPolicies = new TreeMap<>();
  }

  @Nonnull
  public Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> getAggregateNetworks() {
    return _aggregateNetworks;
  }

  public @Nonnull Map<Prefix, BgpNetwork> getNetworks() {
    return _networks;
  }

  public @Nonnull Map<FrrRoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }
}
