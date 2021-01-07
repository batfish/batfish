package org.batfish.dataplane.protocols;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.StaticRoute;

/** Helper functions implementing logic related to handling of static routes */
public class StaticRouteHelper {

  /**
   * Check if a static route with next hop IP can be activated. If this method returns True, an
   * attempt should be made to merge it into the RIB. If it returns false, an attempt should be made
   * to remove it from the RIB.
   *
   * @param route a {@link StaticRoute} to check
   * @param rib the RIB to use for establishing routabilitity to next hop IP
   */
  public static <R extends AbstractRouteDecorator> boolean shouldActivateNextHopIpRoute(
      @Nonnull StaticRoute route, @Nonnull GenericRib<R> rib) {
    Set<R> matchingRoutes = rib.longestPrefixMatch(route.getNextHopIp());

    // If matchingRoutes is empty, cannot activate because next hop ip is unreachable
    return matchingRoutes.stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .anyMatch(
            routeToNextHop ->
                // Next hop has to be reachable through a route with a different prefix
                !routeToNextHop.getNetwork().equals(route.getNetwork())
                    || route.equals(routeToNextHop));
  }
}
