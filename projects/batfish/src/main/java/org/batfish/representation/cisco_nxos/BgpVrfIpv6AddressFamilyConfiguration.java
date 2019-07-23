package org.batfish.representation.cisco_nxos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.Prefix6;

/**
 * Represents the BGP configuration for a IPv4 unicast or multicast address family at the VRF level.
 */
public final class BgpVrfIpv6AddressFamilyConfiguration extends BgpVrfIpAddressFamilyConfiguration {

  public BgpVrfIpv6AddressFamilyConfiguration() {
    _aggregateNetworks = new HashMap<>();
    _networks = new HashMap<>();
  }

  public Map<Prefix6, BgpVrfAddressFamilyAggregateNetworkConfiguration> getAggregateNetworks() {
    return Collections.unmodifiableMap(_aggregateNetworks);
  }

  public BgpVrfAddressFamilyAggregateNetworkConfiguration getOrCreateAggregateNetwork(
      Prefix6 prefix) {
    return _aggregateNetworks.computeIfAbsent(
        prefix, p -> new BgpVrfAddressFamilyAggregateNetworkConfiguration());
  }

  public Map<Prefix6, String> getNetworks() {
    return Collections.unmodifiableMap(_networks);
  }

  public void addNetwork(Prefix6 prefix, String routeMapNameOrEmpty) {
    _networks.put(prefix, routeMapNameOrEmpty);
  }

  private final Map<Prefix6, BgpVrfAddressFamilyAggregateNetworkConfiguration> _aggregateNetworks;
  private final Map<Prefix6, String> _networks;
}
