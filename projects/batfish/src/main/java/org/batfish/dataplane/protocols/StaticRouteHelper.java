package org.batfish.dataplane.protocols;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.StaticRoute;

/** Helper functions implementing logic related to handling of static routes */
public class StaticRouteHelper {

  /** Determine if given static route is an interface route. */
  public static boolean isInterfaceRoute(StaticRoute route) {
    return !Route.UNSET_NEXT_HOP_INTERFACE.equals(route.getNextHopInterface());
  }

  /**
   * Check if a static route with next hop IP can be activated. If this method returns True, an
   * attempt should be made to merge it into the RIB. If it returns false, an attempt should be made
   * to remove it from the RIB.
   *
   * @param route a {@link StaticRoute} to check
   * @param rib the RIB to use for establishing routabilitity to next hop IP
   */
  public static boolean shouldActivateNextHopIpRoute(
      @Nonnull StaticRoute route, @Nonnull GenericRib<AbstractRoute> rib) {
    Set<AbstractRoute> matchingRoutes = rib.longestPrefixMatch(route.getNextHopIp());

    if (matchingRoutes.isEmpty()) {
      // Cannot activate, next hop ip is unreachable
      return false;
    }

    boolean shouldActivate = false;
    for (AbstractRoute routeToNextHop : matchingRoutes) {
      if (!routeToNextHop.getNetwork().equals(route.getNetwork()) || route.equals(routeToNextHop)) {
        // Next hop has to be reachable through a route with a different prefix
        shouldActivate = true;
        break;
      }
    }
    return shouldActivate;
  }
}
