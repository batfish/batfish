package org.batfish.dataplane.protocols;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.isis.IsisLevel;
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
  public static Optional<IsisRoute> convertRouteLevel1ToLevel2(IsisRoute route, int l2Admin) {
    if (route.getLevel() != IsisLevel.LEVEL_1 || route.getAttach() || route.getDown()) {
      return Optional.empty();
    }

    return Optional.of(
        route
            .toBuilder()
            .setAdmin(l2Admin)
            .setLevel(IsisLevel.LEVEL_2)
            .setProtocol(RoutingProtocol.ISIS_L2)
            .build());
  }

  @Nonnull
  public static RibDelta<IsisRoute> setOverloadOnAllRoutes(@Nonnull RibDelta<IsisRoute> delta) {

    RibDelta.Builder<IsisRoute> deltaWithOverloadTrue = RibDelta.builder(AbstractRoute::getNetwork);
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
}
