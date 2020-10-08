package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.AbstractRib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

@ParametersAreNonnullByDefault
public final class RouteDependencyTracker<
    R extends AbstractRouteDecorator, D extends AbstractRouteDecorator> {

  /** Map of routes to the set of routes they depend on */
  @Nonnull private Map<D, Set<R>> _routeDependents;

  RouteDependencyTracker() {
    _routeDependents = new IdentityHashMap<>();
  }

  /**
   * Add a dependency between two routes (e.g., an aggregate route can depend on one or more
   * contributing routes)
   *
   * @param route the dependant route
   * @param dependsOn the dependency
   */
  @VisibleForTesting
  void addRouteDependency(R route, D dependsOn) {
    _routeDependents.computeIfAbsent(dependsOn, r -> new HashSet<>());
    _routeDependents.get(dependsOn).add(route);
  }

  /**
   * Remove the dependencies of a given route from the RIB. Note this call performs a cascading
   * delete (e.g., removes dependencies of dependencies)
   *
   * @param route route whose dependencies we should remove
   * @param rib the RIB containing the dependencies
   * @return a {@link RibDelta} containing all removed dependencies
   */
  @Nonnull
  RibDelta<R> deleteRoute(D route, AbstractRib<R> rib) {
    Set<R> dependents = _routeDependents.get(route);
    if (dependents == null) {
      // Nothing to process
      return RibDelta.empty();
    }
    RibDelta.Builder<R> b = RibDelta.builder();
    for (R depRoute : dependents) {
      b.from(rib.removeRouteGetDelta(depRoute, Reason.WITHDRAW));
    }
    // Now the route is gone and will have no dependents
    _routeDependents.remove(route);

    return b.build();
  }
}
