package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;

/**
 * Contains information about the {@link Route}s which led to the selection of the outgoing
 * interface for the {@link ExitOutputIfaceStep}
 */
public class RouteInfo {

  private static final String PROP_TYPE = "type";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";

  /** Type of the route like BGP, OSPF etc. */
  private final String _type;

  /** Network of this route */
  private final Prefix _network;

  /** Next Hop IP for this route */
  private @Nullable final Ip _nextHopIp;

  public RouteInfo(String type, Prefix network, @Nullable Ip nextHopIp) {
    _type = type;
    _network = network;
    _nextHopIp = nextHopIp;
  }

  @JsonCreator
  private static RouteInfo jsonCreator(
      @JsonProperty(PROP_TYPE) @Nullable String type,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp) {
    checkArgument(type != null, "Route type should be present");
    checkArgument(network != null, "Network should exist in a route");
    return new RouteInfo(type, network, nextHopIp);
  }

  @JsonProperty(PROP_TYPE)
  public String getType() {
    return _type;
  }

  @JsonProperty(PROP_NETWORK)
  public Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }
}
