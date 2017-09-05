package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;

public class RipInternalRoute extends RipRoute {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public RipInternalRoute(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric) {
    super(network, nextHopIp, admin, metric);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    RipInternalRoute other = (RipInternalRoute) obj;
    if (_metric != other._metric) {
      return false;
    }
    if (_admin != other._admin) {
      return false;
    }
    if (!Objects.equals(_nextHopIp, other._nextHopIp)) {
      return false;
    }
    return _network.equals(other._network);
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.RIP;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _admin;
    result = prime * result + Long.hashCode(_metric);
    result = prime * result + _network.hashCode();
    result = prime * result + (_nextHopIp == null ? 0 : _nextHopIp.hashCode());
    return result;
  }

  @Override
  protected final String protocolRouteString() {
    return "";
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    /*
     * TODO: In case we implement RipExternalRoute or something like that, we need class
     * comparison. Need to remove if that won't happen.
     */
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    return 0;
  }
}
