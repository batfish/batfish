package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.Community;

/**
 * A generic EVPN route containing the common properties among the different types of EVPN routes
 */
@ParametersAreNonnullByDefault
public abstract class EvpnRoute extends BgpRoute {

  /** Builder for {@link EvpnRoute} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends EvpnRoute>
      extends BgpRoute.Builder<B, R> {

    @Nullable protected RouteDistinguisher _routeDistinguisher;

    @Nullable
    public RouteDistinguisher getRouteDistinguisher() {
      return _routeDistinguisher;
    }

    public B setRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
      _routeDistinguisher = routeDistinguisher;
      return getThis();
    }
  }

  private static final long serialVersionUID = 1L;

  protected static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  @Nonnull protected final RouteDistinguisher _routeDistinguisher;

  protected EvpnRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      AsPath asPath,
      SortedSet<Community> communities,
      boolean discard,
      long localPreference,
      long med,
      String nextHopInterface,
      Ip originatorIp,
      SortedSet<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginType originType,
      RoutingProtocol protocol,
      Ip receivedFromIp,
      RoutingProtocol srcProtocol,
      int weight,
      boolean nonForwarding,
      boolean nonRouting,
      RouteDistinguisher routeDistinguisher) {
    super(
        network,
        nextHopIp,
        admin,
        asPath,
        communities,
        discard,
        localPreference,
        med,
        nextHopInterface,
        originatorIp,
        clusterList,
        receivedFromRouteReflectorClient,
        originType,
        protocol,
        receivedFromIp,
        srcProtocol,
        weight,
        nonForwarding,
        nonRouting);
    _routeDistinguisher = routeDistinguisher;
  }

  @Nonnull
  @JsonProperty(PROP_ROUTE_DISTINGUISHER)
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }
}
