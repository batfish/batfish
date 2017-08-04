package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;

public class ConnectedRoute extends AbstractRoute {

  private static final long serialVersionUID = 1L;

  private final String _nextHopInterface;

  @JsonCreator
  public ConnectedRoute(
      @JsonProperty(NETWORK_VAR) Prefix network,
      @JsonProperty(NEXT_HOP_INTERFACE_VAR) String nextHopInterface) {
    super(network);
    _nextHopInterface = MoreObjects.firstNonNull(nextHopInterface,Route.UNSET_NEXT_HOP_INTERFACE);
  }

  @Override
  public boolean equals(Object o) {
    ConnectedRoute rhs = (ConnectedRoute) o;
    boolean res = _network.equals(rhs._network);
    return res && _nextHopInterface.equals(rhs._nextHopInterface);
  }

  @Override
  public int getAdministrativeCost() {
    return 0;
  }

  @Override
  public Integer getMetric() {
    return 0;
  }

  // TODO(http://github.com/batfish/batfish/issues/207)
  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(NEXT_HOP_INTERFACE_VAR)
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Override
  public Ip getNextHopIp() {
    return Route.UNSET_ROUTE_NEXT_HOP_IP;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.CONNECTED;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _network.hashCode();
    result = prime * result + _nextHopInterface.hashCode();
    return result;
  }

  @Override
  protected String protocolRouteString() {
    return "";
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    return 0;
  }
}
