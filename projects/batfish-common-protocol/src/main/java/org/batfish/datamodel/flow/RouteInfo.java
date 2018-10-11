package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class RouteInfo {

  private static final String PROP_TYPE = "type";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";

  private final String _type;

  private final Prefix _network;

  private @Nullable final Ip _nextHopIp;

  @JsonCreator
  public RouteInfo(
      @JsonProperty(PROP_TYPE) String type,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp) {
    _type = type;
    _network = network;
    _nextHopIp = nextHopIp;
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
