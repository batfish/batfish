package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;

public class OspfInterAreaRoute extends OspfInternalRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static OspfInterAreaRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_AREA) long area) {
    return new OspfInterAreaRoute(network, nextHopIp, admin, metric, area, false, false);
  }

  OspfInterAreaRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, nonForwarding, nonRouting);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF_IA;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfInterAreaRoute)) {
      return false;
    }
    OspfInterAreaRoute other = (OspfInterAreaRoute) o;
    return Objects.equals(_nextHopIp, other._nextHopIp)
        && _admin == other._admin
        && _area == other._area
        && _metric == other._metric
        && _network.equals(other._network);
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    OspfInterAreaRoute castRhs = (OspfInterAreaRoute) rhs;
    int ret = Long.compare(_area, castRhs._area);
    return ret;
  }
}
