package org.batfish.representation.fortios;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.route.nh.NextHopInterface;

public class FortiosRouteConversions {

  public static @Nonnull SortedSet<org.batfish.datamodel.StaticRoute> convertStaticRoutes(
      Collection<StaticRoute> vsRoutes) {
    return vsRoutes.stream()
        .map(FortiosRouteConversions::convertStaticRoute)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  private static Optional<org.batfish.datamodel.StaticRoute> convertStaticRoute(StaticRoute route) {
    if (!route.getStatusEffective()) {
      return Optional.empty();
    }
    // TODO Should SD-WAN routes be converted? If so how should that affect the VI route?
    checkArgument(route.getDevice() != null, "Route device must be set during extraction");
    return Optional.of(
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(route.getDstEffective())
            // Gateway IP may be null, but that's ok
            .setNextHop(NextHopInterface.of(route.getDevice(), route.getGateway()))
            .setAdmin(route.getDistanceEffective())
            .build());
  }
}
