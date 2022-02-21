package org.batfish.dataplane.ibdp;

import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.tracking.TrackRoute;

/** Utilities for evaluating {@link TrackRoute}. */
public final class TrackRouteUtils {

  /** Evaluates a {@link TrackRoute} given a {@code rib}. */
  public static boolean evaluateTrackRoute(
      TrackRoute trackRoute, GenericRib<AnnotatedRoute<AbstractRoute>> rib) {
    Set<AnnotatedRoute<AbstractRoute>> routesForPrefix = rib.getRoutes(trackRoute.getPrefix());
    if (trackRoute.getProtocols().isEmpty()) {
      return !routesForPrefix.isEmpty();
    } else {
      return routesForPrefix.stream()
          .anyMatch(r -> trackRoute.getProtocols().contains(r.getRoute().getProtocol()));
    }
  }

  private TrackRouteUtils() {}
}
