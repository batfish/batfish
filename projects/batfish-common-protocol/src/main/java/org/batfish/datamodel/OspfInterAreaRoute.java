package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** OSPF inter-area area (can traverse OSPF areas). */
@ParametersAreNonnullByDefault
public final class OspfInterAreaRoute extends OspfInternalRoute {

  private static final Interner<OspfInterAreaRoute> _cache = Interners.newWeakInterner();

  @JsonCreator
  private static OspfInterAreaRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_METRIC) Long metric,
      @Nullable @JsonProperty(PROP_AREA) Long area,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(nextHopIp != null, "%s must be specified", PROP_NEXT_HOP_IP);
    checkArgument(admin != null, "%s must be specified", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "%s must be specified", PROP_METRIC);
    checkArgument(area != null, "%s must be specified", PROP_AREA);
    return new OspfInterAreaRoute(network, nextHopIp, admin, metric, area, tag, false, false);
  }

  private OspfInterAreaRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      long metric,
      long area,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, tag, nonForwarding, nonRouting);
  }

  @Nonnull
  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF_IA;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Allows easy upgrading of {@link OspfIntraAreaRoute intra-area} routes to inter-area routes */
  public static Builder builder(OspfIntraAreaRoute route) {
    return builder()
        // AbstractRoute properties
        .setNetwork(route.getNetwork())
        .setNextHopIp(route.getNextHopIp())
        .setAdmin(route.getAdministrativeCost())
        .setMetric(route.getMetric())
        .setNonForwarding(route.getNonForwarding())
        .setNonRouting(route.getNonRouting())
        .setTag(route.getTag())
        // OspfInterAreaRoute properties
        .setArea(route.getArea());
  }

  public static final class Builder extends AbstractRouteBuilder<Builder, OspfInterAreaRoute> {

    private Long _area;

    @Nonnull
    @Override
    public OspfInterAreaRoute build() {
      return _cache.intern(
          new OspfInterAreaRoute(
              getNetwork(),
              getNextHopIp(),
              getAdmin(),
              getMetric(),
              _area,
              getTag(),
              getNonForwarding(),
              getNonRouting()));
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

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        // AbstractRoute properties
        .setNetwork(getNetwork())
        .setNextHopIp(getNextHopIp())
        .setAdmin(getAdministrativeCost())
        .setMetric(getMetric())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setTag(_tag)
        // OspfInterAreaRoute properties
        .setArea(getArea());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfInterAreaRoute)) {
      return false;
    }
    OspfInterAreaRoute other = (OspfInterAreaRoute) o;
    return _network.equals(other._network)
        && _admin == other._admin
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && _area == other._area
        && _metric == other._metric
        && _nextHopIp.equals(other._nextHopIp)
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network, _admin, _area, _metric, _nextHopIp, getNonForwarding(), getNonRouting(), _tag);
  }
}
