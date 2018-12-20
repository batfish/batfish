package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** OSPF intra-area route. Must stay within a single OSPF area. */
public class OspfIntraAreaRoute extends OspfInternalRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static OspfIntraAreaRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_AREA) long area) {
    return new OspfIntraAreaRoute(network, nextHopIp, admin, metric, area, false, false);
  }

  OspfIntraAreaRoute(
      Prefix network,
      @Nullable Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, nonForwarding, nonRouting);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfIntraAreaRoute)) {
      return false;
    }
    OspfIntraAreaRoute other = (OspfIntraAreaRoute) o;
    return Objects.equals(_network, other._network)
        && _admin == other._admin
        && _area == other._area
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && Objects.equals(_metric, other._metric)
        && Objects.equals(_nextHopIp, other._nextHopIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _admin, _area, _metric, _nextHopIp);
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    OspfIntraAreaRoute castRhs = (OspfIntraAreaRoute) rhs;
    return Long.compare(_area, castRhs._area);
  }
}
