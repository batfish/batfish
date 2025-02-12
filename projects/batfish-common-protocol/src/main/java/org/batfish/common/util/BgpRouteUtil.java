package org.batfish.common.util;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;

/** Utility functions for {@link org.batfish.datamodel.BgpRoute} and its implementations. */
public class BgpRouteUtil {
  /**
   * Convert a route that is neither a {@link BgpRoute} nor a {@link GeneratedRoute} to a {@link
   * Bgpv4Route.Builder}.
   *
   * <p>Intended for converting main RIB routes into their BGP equivalents before passing {@code
   * routeDecorator} to the export policy
   *
   * <p>The builder returned will have default local preference, redistribute origin mechanism,
   * incomplete origin type, and most other fields unset.
   */
  public static @Nonnull Bgpv4Route.Builder convertNonBgpRouteToBgpRoute(
      AbstractRouteDecorator routeDecorator,
      Ip routerId,
      Ip nextHopIp,
      int adminDistance,
      RoutingProtocol protocol,
      OriginMechanism originMechanism) {
    assert protocol == RoutingProtocol.BGP || protocol == RoutingProtocol.IBGP;
    assert !(routeDecorator.getAbstractRoute() instanceof BgpRoute);
    AbstractRoute route = routeDecorator.getAbstractRoute();
    return Bgpv4Route.builder()
        .setNetwork(route.getNetwork())
        .setAdmin(adminDistance)
        .setOriginatorIp(routerId)
        .setProtocol(protocol)
        .setSrcProtocol(route.getProtocol())
        .setOriginMechanism(originMechanism)
        .setOriginType(OriginType.INCOMPLETE)
        // TODO: support customization of route preference
        .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
        .setReceivedFrom(/* Originated locally. */ ReceivedFromSelf.instance())
        .setNextHopIp(nextHopIp)
        .setMetric(route.getMetric())
        .setTag(routeDecorator.getAbstractRoute().getTag());
    // Let everything else default to unset/empty/etc.
  }
}
