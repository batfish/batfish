package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

/** BGP configuration of one VRF: the global BGP mode, or a {@code vrf <name>} BGP-VRF mode. */
public final class NecIxBgpVrf implements Serializable {

  /** {@code originate-default [always]}. */
  public enum OriginateDefault {
    /** Not configured: the default route is not advertised. */
    NONE,
    /** Advertised only if the routing table has a default route. */
    CONDITIONAL,
    /** Always advertised. */
    ALWAYS,
  }

  /** {@code redistribute {connected|static} [metric <m>] [route-map <map>]}. */
  public static final class Redistribution implements Serializable {
    private final @Nonnull RoutingProtocol _protocol;
    private final @Nullable Long _metric;
    private final @Nullable String _routeMap;

    public Redistribution(
        RoutingProtocol protocol, @Nullable Long metric, @Nullable String routeMap) {
      _protocol = protocol;
      _metric = metric;
      _routeMap = routeMap;
    }

    public @Nonnull RoutingProtocol getProtocol() {
      return _protocol;
    }

    public @Nullable Long getMetric() {
      return _metric;
    }

    public @Nullable String getRouteMap() {
      return _routeMap;
    }
  }

  private final @Nonnull Map<Ip, NecIxBgpNeighbor> _neighbors;
  private final @Nonnull List<Prefix> _networks;
  private final @Nonnull List<Redistribution> _redistributions;
  private @Nonnull OriginateDefault _originateDefault;

  public NecIxBgpVrf() {
    _neighbors = new LinkedHashMap<>();
    _networks = new ArrayList<>();
    _redistributions = new ArrayList<>();
    _originateDefault = OriginateDefault.NONE;
  }

  public @Nonnull Map<Ip, NecIxBgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nonnull NecIxBgpNeighbor getOrCreateNeighbor(Ip address) {
    return _neighbors.computeIfAbsent(address, NecIxBgpNeighbor::new);
  }

  /** Prefixes from {@code network}, advertised only while they are in the routing table. */
  public @Nonnull List<Prefix> getNetworks() {
    return _networks;
  }

  public @Nonnull List<Redistribution> getRedistributions() {
    return _redistributions;
  }

  public @Nonnull OriginateDefault getOriginateDefault() {
    return _originateDefault;
  }

  public void setOriginateDefault(OriginateDefault originateDefault) {
    _originateDefault = originateDefault;
  }
}
