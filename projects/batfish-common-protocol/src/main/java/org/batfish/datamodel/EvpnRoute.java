package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/**
 * A generic EVPN route containing the common properties among the different types of EVPN routes
 */
@ParametersAreNonnullByDefault
public abstract class EvpnRoute<B extends Builder<B, R>, R extends BgpRoute<B, R>>
    extends BgpRoute<B, R> {

  /** Builder for {@link EvpnRoute} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends EvpnRoute<B, R>>
      extends BgpRoute.Builder<B, R> {

    @Nullable protected RouteDistinguisher _routeDistinguisher;
    @Nullable protected Integer _vni;

    @Override
    public final B setAdmin(int admin) {
      // All EVPN routes have admin set to an arbitrary constant value because they never compete
      // with non-EVPN routes
      throw new UnsupportedOperationException("Cannot set admin for an EvpnRoute");
    }

    @Nonnull
    @Override
    public final B setNonRouting(boolean nonRouting) {
      // All EVPN routes have nonrouting set to true (they should never enter the main RIB)
      throw new UnsupportedOperationException("Cannot set nonRouting for an EvpnRoute");
    }

    @Nonnull
    @Override
    public final B setNonForwarding(boolean nonForwarding) {
      // All EVPN routes have nonforwarding set to true (they should never enter the main RIB)
      throw new UnsupportedOperationException("Cannot set nonForwarding for an EvpnRoute");
    }

    @Nullable
    public RouteDistinguisher getRouteDistinguisher() {
      return _routeDistinguisher;
    }

    public B setRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
      _routeDistinguisher = routeDistinguisher;
      return getThis();
    }

    public B setVni(Integer vni) {
      _vni = vni;
      return getThis();
    }

    @Nonnull
    @Override
    public abstract R build();
  }

  /**
   * Admin distance to use for all EVPN routes; value doesn't matter since it's the same for all
   * EVPN routes, and they never compete against non-EVPN routes.
   */
  static final @VisibleForTesting int EVPN_ADMIN = 0;

  static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  static final String PROP_VNI = "vni";

  @Nonnull protected final RouteDistinguisher _routeDistinguisher;
  @Nonnull protected final int _vni;

  protected EvpnRoute(
      Prefix network,
      NextHop nextHop,
      AsPath asPath,
      CommunitySet communities,
      long localPreference,
      long med,
      Ip originatorIp,
      Set<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginMechanism originMechanism,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      @Nullable RoutingProtocol srcProtocol,
      long tag,
      int weight,
      RouteDistinguisher routeDistinguisher,
      int vni) {
    super(
        network,
        nextHop,
        EVPN_ADMIN,
        asPath,
        communities,
        localPreference,
        med,
        originatorIp,
        clusterList,
        receivedFromRouteReflectorClient,
        originMechanism,
        originType,
        protocol,
        receivedFromIp,
        srcProtocol,
        tag,
        weight,
        true,
        true);
    _routeDistinguisher = routeDistinguisher;
    _vni = vni;
  }

  @Nonnull
  @JsonProperty(PROP_ROUTE_DISTINGUISHER)
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  /** Return extended communities that are route targets for this route */
  @JsonIgnore
  public Set<ExtendedCommunity> getRouteTargets() {
    return _communities.getExtendedCommunities().stream()
        .filter(ExtendedCommunity::isRouteTarget)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public abstract B toBuilder();
}
