package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;

/** OSPF intra-area route. Must stay within a single OSPF area. */
@ParametersAreNonnullByDefault
public class OspfIntraAreaRoute extends OspfInternalRoute {

  private static final Interner<OspfIntraAreaRoute> _cache = Interners.newWeakInterner();
  private int _hashCode;

  @JsonCreator
  private static OspfIntraAreaRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(nextHopIp != null, "%s must be specified", PROP_NEXT_HOP_IP);
    checkArgument(admin != null, "%s must be specified", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "%s must be specified", PROP_METRIC);
    checkArgument(area != null, "%s must be specified", PROP_AREA);
    return new OspfIntraAreaRoute(
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        admin,
        metric,
        area,
        tag,
        false,
        false);
  }

  public OspfIntraAreaRoute(
      Prefix network,
      NextHop nextHop,
      int admin,
      long metric,
      long area,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHop, admin, metric, area, tag, nonForwarding, nonRouting);
  }

  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends AbstractRouteBuilder<Builder, OspfIntraAreaRoute> {

    private long _area;

    @Override
    public @Nonnull OspfIntraAreaRoute build() {
      checkArgument(_nextHop != null);
      OspfIntraAreaRoute r =
          new OspfIntraAreaRoute(
              getNetwork(),
              _nextHop,
              getAdmin(),
              getMetric(),
              _area,
              getTag(),
              getNonForwarding(),
              getNonRouting());
      return _cache.intern(r);
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public Builder setArea(long area) {
      _area = area;
      return this;
    }

    private Builder() {}
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        // AbstractRoute properties
        .setNetwork(getNetwork())
        .setNextHop(_nextHop)
        .setAdmin(_admin)
        .setMetric(_metric)
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setTag(_tag)
        // OspfIntraAreaRoute properties
        .setArea(getArea());
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
        && _nextHop.equals(other._nextHop)
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _network.hashCode();
      h = 31 * h + _admin;
      h = 31 * h + Long.hashCode(_area);
      h = 31 * h + Long.hashCode(_metric);
      h = 31 * h + _nextHop.hashCode();
      h = 31 * h + Boolean.hashCode(getNonForwarding());
      h = 31 * h + Boolean.hashCode(getNonRouting());
      h = 31 * h + Long.hashCode(_tag);

      _hashCode = h;
    }
    return h;
  }
}
