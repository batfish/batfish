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
   * The routes in {@code rib} that a static route with next hop {@code nextHopIp} may resolve
   * through: the longest prefix matches that are connected or, if {@code recursive}, pass {@code
   * restriction}. Shared by every static route with the same next hop IP and recursiveness, so a
   * caller with many such routes can resolve once.
   */
  public static <R extends AbstractRouteDecorator> Set<R> resolveStaticNextHopIp(
      Ip nextHopIp, boolean recursive, GenericRib<R> rib, ResolutionRestriction<R> restriction) {
    return rib.longestPrefixMatch(
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
  }

  /**
   * Whether {@code route} can be activated given {@code matchingRoutes}, the result of {@link
   * #resolveStaticNextHopIp} for its next hop IP and recursiveness.
   */
  public static <R extends AbstractRouteDecorator> boolean shouldActivateNextHopIpRoute(
      StaticRoute route, Set<R> matchingRoutes) {
    Ip nextHopIp = route.getNextHopIp();
    // - If matchingRoutes is empty, cannot activate because the next hop ip is unreachable.
    // - If the prefix of the route to be activated contains the route's next hop, then
    //   a matching route must have a longer prefix. Otherwise, the route will become its own
    //   longest prefix match upon activation, creating a loop.
    Prefix network = route.getNetwork();
    int prefixLength = network.getPrefixLength();
    boolean containsOwnNextHop = network.containsIp(nextHopIp);
    if (!containsOwnNextHop) {
      return !matchingRoutes.isEmpty();
    }
    for (R routeToNextHop : matchingRoutes) {
      if (routeToNextHop.getNetwork().getPrefixLength() > prefixLength) {
        return true;
      }
    }
    return false;
  }
}
