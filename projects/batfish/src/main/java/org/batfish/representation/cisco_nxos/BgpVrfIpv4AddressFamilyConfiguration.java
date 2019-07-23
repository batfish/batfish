package org.batfish.representation.cisco_nxos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.Prefix;

/**
 * Represents the BGP configuration for a IPv4 unicast or multicast address family at the VRF level.
 */
public final class BgpVrfIpv4AddressFamilyConfiguration extends BgpVrfIpAddressFamilyConfiguration {

  public BgpVrfIpv4AddressFamilyConfiguration() {
    _aggregateNetworks = new HashMap<>();
    _ipNetworks = new HashMap<>();
  }

  public Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> getAggregateNetworks() {
    return Collections.unmodifiableMap(_aggregateNetworks);
  }

  public BgpVrfAddressFamilyAggregateNetworkConfiguration getOrCreateAggregateNetwork(
      Prefix prefix) {
    return _aggregateNetworks.computeIfAbsent(
        prefix, p -> new BgpVrfAddressFamilyAggregateNetworkConfiguration());
  }

  public Map<Prefix, String> getIpNetworks() {
    return Collections.unmodifiableMap(_ipNetworks);
  }

  public void addIpNetwork(Prefix prefix, String routeMapNameOrEmpty) {
    _ipNetworks.put(prefix, routeMapNameOrEmpty);
  }

  private final Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> _aggregateNetworks;
  private final Map<Prefix, String> _ipNetworks;
}
