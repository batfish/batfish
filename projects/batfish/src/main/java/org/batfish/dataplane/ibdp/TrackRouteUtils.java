package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.tracking.TrackRoute;

/** Utilities for evaluating {@link TrackRoute}. */
public final class TrackRouteUtils {

  /**
   * A function that returns a {@link Set} of routes of generic type {@code R} given a {@link
   * Prefix}.
   */
  @FunctionalInterface
  public interface GetRoutesForPrefix<R extends AbstractRouteDecorator> {
    Set<R> getRoutesForPrefix(Prefix p);
  }

  /**
   * Evaluates a {@link TrackRoute} given a function that returns routes for a given {@link Prefix}.
   */
  public static <R extends AbstractRouteDecorator> boolean evaluateTrackRoute(
      TrackRoute trackRoute, GetRoutesForPrefix<R> getRoutesForPrefix) {
    Set<R> routesForPrefix = getRoutesForPrefix.getRoutesForPrefix(trackRoute.getPrefix());
    if (trackRoute.getProtocols().isEmpty()) {
      return !routesForPrefix.isEmpty();
    } else {
      return routesForPrefix.stream()
          .anyMatch(r -> trackRoute.getProtocols().contains(r.getAbstractRoute().getProtocol()));
    }
  }

  /** A {@link GetRoutesForPrefix} that returns the empty set for all inputs. */
  public static <R extends AbstractRouteDecorator> Set<R> emptyGetRoutesForPrefix(Prefix p) {
    return ImmutableSet.of();
  }

  private TrackRouteUtils() {}
}
