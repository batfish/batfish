package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix6;

/**
 * Represents the BGP configuration for a IPv4 unicast or multicast address family at the VRF level.
 */
public final class BgpVrfIpv6AddressFamilyConfiguration extends BgpVrfIpAddressFamilyConfiguration {
  public static class Network implements Serializable {
    public Network(Prefix6 network, @Nullable String routeMap) {
      _network = network;
      _routeMap = routeMap;
    }

    public @Nonnull Prefix6 getNetwork() {
      return _network;
    }

    public @Nullable String getRouteMap() {
      return _routeMap;
    }

    private final @Nonnull Prefix6 _network;
    private final @Nullable String _routeMap;
  }

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

  public Collection<Network> getNetworks() {
    return _networks.values();
  }

  public void addNetwork(Prefix6 prefix, @Nullable String routeMap) {
    _networks.put(prefix, new Network(prefix, routeMap));
  }

  private final Map<Prefix6, BgpVrfAddressFamilyAggregateNetworkConfiguration> _aggregateNetworks;
  private final Map<Prefix6, Network> _networks;
}
