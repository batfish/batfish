package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.dataplane.rib.BgpRib;
import org.batfish.dataplane.rib.BgpRib.MultipathRibDelta;
import org.batfish.dataplane.rib.RibDelta;

/** Keeps track of BGP routes that depend on routes in the main RIB */
@ParametersAreNonnullByDefault
public final class BgpRouteDependencyTracker<
    R extends BgpRoute<?, ?>, D extends AbstractRouteDecorator> {

  /** Map of routes to the set of routes they depend on */
  @Nonnull private final Map<D, Set<R>> _routeDependents;

  BgpRouteDependencyTracker() {
    _routeDependents = new IdentityHashMap<>();
  }

  /**
   * Add a dependency between two routes (e.g., a BGP aggregate route can depend on one or more
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
   * @return a {@link MultipathRibDelta} containing all removed dependencies
   */
  @Nonnull
  MultipathRibDelta<R> deleteRoute(D route, BgpRib<R> rib) {
    Set<R> dependents = _routeDependents.get(route);
    if (dependents == null) {
      // Nothing to process
      return new MultipathRibDelta<>(RibDelta.empty(), RibDelta.empty());
    }
    RibDelta.Builder<R> bestPathDelta = RibDelta.builder();
    RibDelta.Builder<R> multipathDelta = RibDelta.builder();
    for (R depRoute : dependents) {
      MultipathRibDelta<R> delta = rib.multipathRemoveRouteGetDelta(depRoute);
      bestPathDelta.from(delta.getBestPathDelta());
      multipathDelta.from(delta.getMultipathDelta());
    }
    // Now the route is gone and will have no dependents
    _routeDependents.remove(route);

    return new MultipathRibDelta<>(bestPathDelta.build(), multipathDelta.build());
  }
}
