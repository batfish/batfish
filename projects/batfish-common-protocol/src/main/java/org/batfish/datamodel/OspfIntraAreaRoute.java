package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;

public class OspfIntraAreaRoute extends OspfRoute {

  private static final String PROP_AREA = "area";

  /** */
  private static final long serialVersionUID = 1L;

  private final long _area;

  @JsonCreator
  public OspfIntraAreaRoute(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) int metric,
      @JsonProperty(PROP_AREA) long area) {
    super(network, nextHopIp, admin, metric);
    _area = area;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    OspfIntraAreaRoute other = (OspfIntraAreaRoute) obj;
    if (_nextHopIp == null) {
      if (other._nextHopIp != null) {
        return false;
      }
    } else if (!_nextHopIp.equals(other._nextHopIp)) {
      return false;
    }
    if (_admin != other._admin) {
      return false;
    }
    if (_area != other._area) {
      return false;
    }
    if (_metric != other._metric) {
      return false;
    }
    return _network.equals(other._network);
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  // TODO(http://github.com/batfish/batfish/issues/207)
  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF;
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
    result = prime * result + (int) (_area ^ (_area >>> 32));
    result = prime * result + _metric;
    result = prime * result + _network.hashCode();
    result = prime * result + (_nextHopIp == null ? 0 : _nextHopIp.hashCode());
    return result;
  }

  @Override
  protected final String protocolRouteString() {
    return " area:" + _area;
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    OspfIntraAreaRoute castRhs = (OspfIntraAreaRoute) rhs;
    int ret = Long.compare(_area, castRhs._area);
    return ret;
  }
}
