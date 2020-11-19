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

/**
 * Contains information about the {@link Route}s which led to the selection of the outgoing
 * interface for the {@link ExitOutputIfaceStep}
 */
public final class RouteInfo {
  private static final String PROP_PROTOCOL = "protocol";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";
  private static final String PROP_NEXT_VRF = "nextVrf";

  /** Protocol of the route like bgp, ospf etc. */
  private @Nonnull final RoutingProtocol _protocol;

  /** Network of this route */
  private @Nonnull final Prefix _network;

  /** Next Hop IP for this route */
  private @Nullable final Ip _nextHopIp;

  /** Next VRF for this route */
  private @Nullable final String _nextVrf;

  public RouteInfo(
      RoutingProtocol protocol, Prefix network, @Nullable Ip nextHopIp, @Nullable String nextVrf) {
    _protocol = protocol;
    _network = network;
    _nextHopIp = nextHopIp;
    _nextVrf = nextVrf;
  }

  @JsonCreator
  private static RouteInfo jsonCreator(
      @JsonProperty(PROP_PROTOCOL) @Nullable RoutingProtocol protocol,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_VRF) @Nullable String nextVrf) {
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    return new RouteInfo(protocol, network, nextHopIp, nextVrf);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RouteInfo)) {
      return false;
    }
    RouteInfo other = (RouteInfo) o;
    return _protocol.equals(other._protocol)
        && _network.equals(other._network)
        && Objects.equals(_nextHopIp, other._nextHopIp)
        && Objects.equals(_nextVrf, other._nextVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocol, _network, _nextHopIp, _nextVrf);
  }

  @JsonProperty(PROP_PROTOCOL)
  @Nonnull
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(PROP_NETWORK)
  @Nonnull
  public Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(PROP_NEXT_VRF)
  @Nullable
  public String getNextVrf() {
    return _nextVrf;
  }
}
