package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** OSPF iter-area area (can traverse OSPF areas). */
public class OspfInterAreaRoute extends OspfInternalRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static OspfInterAreaRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_AREA) long area) {
    return new OspfInterAreaRoute(network, nextHopIp, admin, metric, area, false, false);
  }

  OspfInterAreaRoute(
      @Nullable Prefix network,
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
    return Objects.equals(_network, other._network)
        && _admin == other._admin
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && _area == other._area
        && _metric == other._metric
        && _nextHopIp.equals(other._nextHopIp);
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
    OspfInterAreaRoute castRhs = (OspfInterAreaRoute) rhs;
    return Comparator.comparing(OspfInterAreaRoute::getArea).compare(this, castRhs);
  }
}
