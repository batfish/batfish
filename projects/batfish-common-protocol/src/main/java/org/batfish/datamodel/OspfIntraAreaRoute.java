package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** OSPF intra-area route. Must stay within a single OSPF area. */
@ParametersAreNonnullByDefault
public class OspfIntraAreaRoute extends OspfInternalRoute {

  private static final long serialVersionUID = 1L;
  private static final Interner<OspfIntraAreaRoute> _cache = Interners.newWeakInterner();
  private int _hashCode;

  @JsonCreator
  private static OspfIntraAreaRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_METRIC) Long metric,
      @Nullable @JsonProperty(PROP_AREA) Long area) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(nextHopIp != null, "%s must be specified", PROP_NEXT_HOP_IP);
    checkArgument(admin != null, "%s must be specified", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "%s must be specified", PROP_METRIC);
    checkArgument(area != null, "%s must be specified", PROP_AREA);
    return new OspfIntraAreaRoute(network, nextHopIp, admin, metric, area, false, false);
  }

  public OspfIntraAreaRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, nonForwarding, nonRouting);
  }

  @Nonnull
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
    return _network.equals(other._network)
        && _admin == other._admin
        && _area == other._area
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && _metric == other._metric
        && _nextHopIp.equals(other._nextHopIp);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _network.hashCode();
      h = 31 * h + _admin;
      h = 31 * h + Long.hashCode(_area);
      h = 31 * h + Long.hashCode(_metric);
      h = 31 * h + _nextHopIp.hashCode();

      _hashCode = h;
    }
    return h;
  }

  @Override
  public Builder toBuilder() {
    return builder()
        // AbstractRoute properties
        .setNetwork(getNetwork())
        .setNextHopIp(getNextHopIp())
        .setAdmin(_admin)
        .setMetric(_metric)
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        // OspfIntraAreaRoute properties
        .setArea(getArea());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends AbstractRouteBuilder<Builder, OspfIntraAreaRoute> {

    private long _area;

    @Nonnull
    @Override
    public OspfIntraAreaRoute build() {
      OspfIntraAreaRoute r =
          new OspfIntraAreaRoute(
              getNetwork(),
              getNextHopIp(),
              getAdmin(),
              getMetric(),
              _area,
              getNonForwarding(),
              getNonRouting());
      return _cache.intern(r);
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setArea(long area) {
      _area = area;
      return this;
    }

    private Builder() {}
  }
}
