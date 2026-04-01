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

/** OSPF inter-area area (can traverse OSPF areas). */
@ParametersAreNonnullByDefault
public final class OspfInterAreaRoute extends OspfInternalRoute {

  /* Cache the hashcode */
  private transient int _hashCode;
  private static final Interner<OspfInterAreaRoute> _cache = Interners.newWeakInterner();

  @JsonCreator
  private static OspfInterAreaRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(admin != null, "%s must be specified", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "%s must be specified", PROP_METRIC);
    checkArgument(area != null, "%s must be specified", PROP_AREA);
    return new OspfInterAreaRoute(
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        admin,
        metric,
        area,
        tag,
        false,
        false);
  }

  private OspfInterAreaRoute(
      Prefix network,
      NextHop nextHop,
      long admin,
      long metric,
      long area,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHop, admin, metric, area, tag, nonForwarding, nonRouting);
  }

  @Override
  public @Nonnull RoutingProtocol getProtocol() {
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
        .setNextHop(route.getNextHop())
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

    @Override
    public @Nonnull OspfInterAreaRoute build() {
      checkArgument(_nextHop != null);
      return _cache.intern(
          new OspfInterAreaRoute(
              getNetwork(),
              _nextHop,
              getAdmin(),
              getMetric(),
              _area,
              getTag(),
              getNonForwarding(),
              getNonRouting()));
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
        .setAdmin(getAdministrativeCost())
        .setMetric(getMetric())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setTag(getTag())
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
        && getAdministrativeCost() == other.getAdministrativeCost()
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && getArea() == other.getArea()
        && getMetric() == other.getMetric()
        && _nextHop.equals(other._nextHop)
        && getTag() == other.getTag();
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _network.hashCode();
      h = 31 * h + Long.hashCode(getAdministrativeCost());
      h = 31 * h + Long.hashCode(getArea());
      h = 31 * h + Long.hashCode(getMetric());
      h = 31 * h + _nextHop.hashCode();
      h = 31 * h + Boolean.hashCode(getNonForwarding());
      h = 31 * h + Boolean.hashCode(getNonRouting());
      h = 31 * h + Long.hashCode(getTag());
      _hashCode = h;
    }
    return h;
  }
}
