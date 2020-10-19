package org.batfish.dataplane.protocols;

import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.dataplane.rib.RibDelta;

/** Helper class that implements various IS-IS protocol logic. */
public class IsisProtocolHelper {

  /**
   * Convert a level-1 IS-IS route to a level-2 IS-IS route. All route attributes except
   * level/protocol and the admin distance remain unchanged.
   *
   * @param route the route to convert
   * @param l2Admin the new L2 admin distance to use.
   * @return an optional of {@link IsisRoute}. It will be empty if the route given is not level 1 or
   *     has its down bit set.
   */
  public static Optional<IsisRoute> convertRouteLevel1ToLevel2(
      IsisRoute route, RoutingProtocol l2Protocol, int l2Admin) {
    if (route.getLevel() != IsisLevel.LEVEL_1 || route.getAttach() || route.getDown()) {
      return Optional.empty();
    }
    return Optional.of(
        route.toBuilder()
            .setAdmin(l2Admin)
            .setLevel(IsisLevel.LEVEL_2)
            .setProtocol(l2Protocol)
            .build());
  }

  @Nonnull
  public static RibDelta<IsisRoute> setOverloadOnAllRoutes(@Nonnull RibDelta<IsisRoute> delta) {

    RibDelta.Builder<IsisRoute> deltaWithOverloadTrue = RibDelta.builder();
    delta
        .getActions()
        .forEach(
            ra -> {
              IsisRoute newRoute = ra.getRoute().toBuilder().setOverload(true).build();
              if (ra.isWithdrawn()) {
                deltaWithOverloadTrue.remove(newRoute, ra.getReason());
              } else {
                deltaWithOverloadTrue.add(newRoute);
              }
            });
    return deltaWithOverloadTrue.build();
  }

  /**
   * Given an {@link AbstractRoute}, run it through IS-IS outbound transformations and export
   * routing policy.
   *
   * @return Transformed {@link IsisRoute} if {@code exportCandidate} passes the IS-IS export
   *     policy; otherwise {@code null}.
   */
  @Nullable
  public static IsisRoute exportNonIsisRouteToIsis(
      @Nonnull AnnotatedRoute<AbstractRoute> exportCandidate,
      @Nonnull IsisProcess process,
      boolean isLevel1,
      Configuration c) {
    // Get export policy if there is one
    RoutingPolicy exportPolicy =
        Optional.ofNullable(process.getExportPolicy())
            .map(policyName -> c.getRoutingPolicies().get(policyName))
            .orElse(null);
    if (exportPolicy == null) {
      // Export policy is undefined or not configured
      return null;
    }
    // Process transformed outgoing route through the export policy
    IsisRoute.Builder transformedOutgoingRouteBuilder =
        convertNonIsisRouteToIsisRoute(exportCandidate.getRoute(), process, isLevel1, c);
    boolean policyAcceptsRoute =
        exportPolicy.process(exportCandidate, transformedOutgoingRouteBuilder, Direction.OUT);

    // If route was accepted by export policy, return it
    return policyAcceptsRoute ? transformedOutgoingRouteBuilder.build() : null;
  }

  /**
   * Convert a non-ISIS route to an {@link IsisRoute.Builder}.
   *
   * <p>Intended for converting main RIB routes into their IS-IS equivalents before passing {@code
   * route} to the export policy.
   *
   * <p>Sets network, admin, metric, area, tag, protocol, level, overload, system ID, and nonRouting
   * (to true, should be cleared on import). Does not set next hop IP, attach, or down.
   */
  @Nonnull
  public static IsisRoute.Builder convertNonIsisRouteToIsisRoute(
      AbstractRoute route, IsisProcess process, boolean isLevel1, Configuration c) {
    assert !(route instanceof IsisRoute);
    RoutingProtocol protocol = isLevel1 ? RoutingProtocol.ISIS_EL1 : RoutingProtocol.ISIS_EL2;
    return IsisRoute.builder()
        .setNetwork(route.getNetwork())
        .setAdmin(protocol.getDefaultAdministrativeCost(c.getConfigurationFormat()))
        .setMetric(route.getMetric())
        .setArea(process.getNetAddress().getAreaIdString())
        .setTag(route.getTag())
        .setProtocol(protocol)
        .setLevel(isLevel1 ? IsisLevel.LEVEL_1 : IsisLevel.LEVEL_2)
        .setOverload(process.getOverload())
        .setNextHopIp(UNSET_ROUTE_NEXT_HOP_IP)
        .setSystemId(process.getNetAddress().getSystemIdString())
        .setNonRouting(true);
  }
}
