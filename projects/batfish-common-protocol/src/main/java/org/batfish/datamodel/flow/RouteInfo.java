package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Contains information about the {@link Route}s which led to the selection of the outgoing
 * interface for the {@link ExitOutputIfaceStep}
 */
public final class RouteInfo {
  private static final String PROP_PROTOCOL = "protocol";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP = "nextHop";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";
  private static final String PROP_NEXT_VRF = "nextVrf";
  private static final String PROP_ADMIN_DISTANCE = "admin";
  private static final String PROP_METRIC = "metric";

  /** Protocol of the route like bgp, ospf etc. */
  private final @Nonnull RoutingProtocol _protocol;

  /** Network of this route */
  private final @Nonnull Prefix _network;

  /** Next Hop for this route */
  private final @Nonnull NextHop _nextHop;

  /** Next Hop IP for this route */
  private final @Nullable Ip _nextHopIp;

  /** Next VRF for this route */
  private final @Nullable String _nextVrf;

  /** Administrative distance for this route */
  private final int _adminDistance;

  /** Metric for this route */
  private final long _metric;

  public RouteInfo(
      RoutingProtocol protocol, Prefix network, NextHop nextHop, int adminDistance, long metric) {
    _protocol = protocol;
    _network = network;
    _nextHop = nextHop;
    _nextHopIp = LegacyNextHops.getNextHopIp(_nextHop).orElse(Route.UNSET_ROUTE_NEXT_HOP_IP);
    _nextVrf = LegacyNextHops.getNextVrf(_nextHop).orElse(null);
    _adminDistance = adminDistance;
    _metric = metric;
  }

  @SuppressWarnings("unused") // until nextHopIp and nextVrf are removed entirely
  @JsonCreator
  private static RouteInfo jsonCreator(
      @JsonProperty(PROP_PROTOCOL) @Nullable RoutingProtocol protocol,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP) @Nullable NextHop nextHop,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_VRF) @Nullable String nextVrf,
      @JsonProperty(PROP_ADMIN_DISTANCE) @Nullable Integer adminDistance,
      @JsonProperty(PROP_METRIC) @Nullable Long metric) {
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    checkArgument(adminDistance != null, "Missing %s", PROP_ADMIN_DISTANCE);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    checkArgument(nextHop != null, "Missing %s", PROP_NEXT_HOP);
    return new RouteInfo(protocol, network, nextHop, adminDistance, metric);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RouteInfo)) {
      return false;
    }
    RouteInfo other = (RouteInfo) o;
    return _protocol == other._protocol
        && _network.equals(other._network)
        && _nextHop.equals(other._nextHop)
        && Objects.equals(_nextHopIp, other._nextHopIp)
        && Objects.equals(_nextVrf, other._nextVrf)
        && _adminDistance == other._adminDistance
        && _metric == other._metric;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _protocol, _network, _nextHop, _nextHopIp, _nextVrf, _adminDistance, _metric);
  }

  @JsonProperty(PROP_PROTOCOL)
  public @Nonnull RoutingProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(PROP_NETWORK)
  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP)
  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(PROP_NEXT_VRF)
  public @Nullable String getNextVrf() {
    return _nextVrf;
  }

  @JsonProperty(PROP_ADMIN_DISTANCE)
  public int getAdminDistance() {
    return _adminDistance;
  }

  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return _metric;
  }
}
