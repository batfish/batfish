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
import org.batfish.datamodel.route.nh.NextHopDiscard;

/** OSPF intra-area route. Must stay within a single OSPF area. */
@ParametersAreNonnullByDefault
public class OspfInternalSummaryRoute extends OspfInternalRoute {

  private static final Interner<OspfInternalSummaryRoute> _cache = Interners.newWeakInterner();
  private int _hashCode;

  @JsonCreator
  private static OspfInternalSummaryRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_TAG) long tag) {
    assert NextHop.legacyConverter(nextHopInterface, nextHopIp).equals(NextHopDiscard.instance());
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(admin != null, "%s must be specified", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "%s must be specified", PROP_METRIC);
    checkArgument(area != null, "%s must be specified", PROP_AREA);
    return new OspfInternalSummaryRoute(network, admin, metric, area, tag);
  }

  public OspfInternalSummaryRoute(Prefix network, long admin, long metric, long area, long tag) {
    super(network, NextHopDiscard.instance(), admin, metric, area, tag, false, false);
  }

  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF_IS;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends AbstractRouteBuilder<Builder, OspfInternalSummaryRoute> {

    private long _area;

    @Override
    public @Nonnull OspfInternalSummaryRoute build() {
      OspfInternalSummaryRoute r =
          new OspfInternalSummaryRoute(getNetwork(), getAdmin(), getMetric(), _area, getTag());
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
        .setNextHop(NextHopDiscard.instance())
        .setAdmin(_admin)
        .setMetric(_metric)
        .setTag(_tag)
        // OspfInternalSummaryRoute properties
        .setArea(getArea());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfInternalSummaryRoute)) {
      return false;
    }
    OspfInternalSummaryRoute other = (OspfInternalSummaryRoute) o;
    return _network.equals(other._network)
        && _admin == other._admin
        && _area == other._area
        && _metric == other._metric
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _network.hashCode();
      h = 31 * h + Long.hashCode(_admin);
      h = 31 * h + Long.hashCode(_area);
      h = 31 * h + Long.hashCode(_metric);
      h = 31 * h + Long.hashCode(_tag);

      _hashCode = h;
    }
    return h;
  }
}
