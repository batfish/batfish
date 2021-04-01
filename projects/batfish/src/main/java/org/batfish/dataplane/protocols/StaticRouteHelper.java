package org.batfish.dataplane.protocols;

import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;

/** Helper functions implementing logic related to handling of static routes */
@ParametersAreNonnullByDefault
public class StaticRouteHelper {

  /**
   * Check if a static route with next hop IP can be activated. If this method returns True, an
   * attempt should be made to merge it into the RIB. If it returns false, an attempt should be made
   * to remove it from the RIB.
   *
   * @param route a {@link StaticRoute} to check
   * @param rib the RIB to use for establishing routabilitity to next hop IP
   * @param restriction predicate that restricts which routes may be used to recursively resolve
   *     next-hops
   */
  public static <R extends AbstractRouteDecorator> boolean shouldActivateNextHopIpRoute(
      StaticRoute route, GenericRib<R> rib, ResolutionRestriction<R> restriction) {
    boolean recursive = route.getRecursive();
    Ip nextHopIp = route.getNextHopIp();
    Set<R> matchingRoutes =
        rib.longestPrefixMatch(
            nextHopIp,
            r -> {
              if (r.getAbstractRoute().getProtocol() == RoutingProtocol.CONNECTED) {
                // All static routes can be activated by a connected route.
                return true;
              }
              if (!recursive) {
                // Non-recursive static routes cannot be activated by non-connected routes.
                return false;
              }
              // Recursive routes must pass restriction if present.
              return restriction.test(r);
            });

    // - If matchingRoutes is empty, cannot activate because the next hop ip is unreachable.
    // - If the prefix of the route to be activated contains the route's next hop, then
    //   a matching route must have a longer prefix. Otherwise, the route will become its own
    //   longest prefix match upon activation, creating a loop.
    Prefix network = route.getNetwork();
    int prefixLength = network.getPrefixLength();
    boolean containsOwnNextHop = network.containsIp(nextHopIp);
    return matchingRoutes.stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .anyMatch(
            routeToNextHop ->
                !containsOwnNextHop
                    || routeToNextHop.getNetwork().getPrefixLength() > prefixLength);
  }
}
