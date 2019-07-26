package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * Represents the BGP configuration for a IPv4 unicast or multicast address family at the VRF level.
 */
public final class BgpVrfIpv4AddressFamilyConfiguration extends BgpVrfIpAddressFamilyConfiguration {
  public static class Network implements Serializable {
    public Network(Prefix network, @Nullable String routeMap) {
      _network = network;
      _routeMap = routeMap;
    }

    public @Nonnull Prefix getNetwork() {
      return _network;
    }

    public @Nullable String getRouteMap() {
      return _routeMap;
    }

    private final @Nonnull Prefix _network;
    private final @Nullable String _routeMap;
  }

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

  public void addNetwork(Prefix prefix, @Nullable String routeMap) {
    _ipNetworks.put(prefix, new Network(prefix, routeMap));
  }

  public Collection<Network> getNetworks() {
    return _ipNetworks.values();
  }

  private final Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> _aggregateNetworks;
  private final Map<Prefix, Network> _ipNetworks;
}
