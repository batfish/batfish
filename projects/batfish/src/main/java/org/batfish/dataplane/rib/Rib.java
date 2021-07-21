package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 * Represents a general RIB, capable of storing routes across different protocols. Uses
 * administrative cost (a.k.a admin distance) to determine route preference.
 */
@ParametersAreNonnullByDefault
public class Rib extends AnnotatedRib<AbstractRoute> implements Serializable {

  /** Encapsulates optional main RIB behavior of enforcing resolvability for active routes. */
  private final class ResolvabilityEnforcer {

    private final @Nonnull DirectedAcyclicGraph<AnnotatedRoute<AbstractRoute>, DefaultEdge>
        _resolutionGraph;
    private final @Nonnull RibResolutionTrie _ribResolutionTrie;
    private final @Nonnull SetMultimap<Ip, AnnotatedRoute<AbstractRoute>> _routesByNextHopIp;

    private ResolvabilityEnforcer() {
      _routesByNextHopIp = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
      _ribResolutionTrie = new RibResolutionTrie();
      _resolutionGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> delta = RibDelta.builder();
      boolean isNextHopIpRoute = isNextHopIpRoute(route);
      if (isNextHopIpRoute) {
        Ip nextHopIp = route.getAbstractRoute().getNextHopIp();
        _routesByNextHopIp.put(nextHopIp, route);
        _ribResolutionTrie.addNextHopIp(nextHopIp);
      }
      RibDelta<AnnotatedRoute<AbstractRoute>> initialDelta = Rib.super.mergeRouteGetDelta(route);
      return delta.from(processSideEffects(route, initialDelta)).build();
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> removeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      if (isNextHopIpRoute(route)) {
        Ip nextHopIp = route.getAbstractRoute().getNextHopIp();
        _routesByNextHopIp.remove(nextHopIp, route);
        if (_routesByNextHopIp.get(nextHopIp).isEmpty()) {
          _ribResolutionTrie.removeNextHopIp(nextHopIp);
        }
      }
      RibDelta<AnnotatedRoute<AbstractRoute>> delta =
          Rib.super.removeRouteGetDelta(route, Reason.WITHDRAW);
      return processSideEffects(route, delta);
    }

    void postProcessDelta(RibDelta<AnnotatedRoute<AbstractRoute>> delta) {
      if (delta.isEmpty()) {
        return;
      }
      Prefix prefix = delta.getPrefixes().findFirst().get();
      if (delta.getActions().count() == 1) {
        // no backups
        if (extractRoutes(prefix).isEmpty()) {
          _ribResolutionTrie.removePrefix(prefix);
        } else {
          _ribResolutionTrie.addPrefix(prefix);
        }
      }
      delta
          .getActions()
          .forEach(
              action -> {
                AnnotatedRoute<AbstractRoute> route = action.getRoute();
                if (action.isWithdrawn()) {
                  _resolutionGraph.removeVertex(route);
                } else {
                  _resolutionGraph.addVertex(route);
                }
              });
    }

    /**
     * Performs longest prefix match on a next hop IP route. If the route contains its own next hop
     * ip and the result does not contain a more specific route, returns the empty set. Otherwise,
     * returns the LPM routes.
     */
    private @Nonnull Set<AnnotatedRoute<AbstractRoute>> lpmIfValid(
        AnnotatedRoute<AbstractRoute> nhipRoute) {
      Set<AnnotatedRoute<AbstractRoute>> lpmRoutes =
          longestPrefixMatch(
              nhipRoute.getRoute().getNextHopIp(), ResolutionRestriction.alwaysTrue());
      if (containsOwnNextHop(nhipRoute)) {
        int prefixLength = nhipRoute.getNetwork().getPrefixLength();
        return lpmRoutes.stream()
                .anyMatch(lpmRoute -> lpmRoute.getNetwork().getPrefixLength() > prefixLength)
            ? lpmRoutes
            : ImmutableSet.of();
      } else {
        return lpmRoutes;
      }
    }

    /**
     * Processes the side-effects of adding/removing a route to/from the RIB. Returns the transitive
     * changes to the RIB. Any next hop IP routes (including the input) that become unresolvable
     * will be deactivated.
     */
    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> processSideEffects(
        AnnotatedRoute<AbstractRoute> route, RibDelta<AnnotatedRoute<AbstractRoute>> initialDelta) {
      if (initialDelta.isEmpty()) {
        return RibDelta.empty();
      }
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> delta =
          RibDelta.<AnnotatedRoute<AbstractRoute>>builder().from(initialDelta);
      postProcessDelta(initialDelta);
      Set<AnnotatedRoute<AbstractRoute>> affectedRoutes = new LinkedHashSet<>();
      affectedRoutes.add(
          initialDelta
              .getActions()
              .filter(action -> action.getRoute().equals(route))
              .map(RouteAdvertisement::getRoute)
              .findFirst()
              .get());
      initialDelta
          .getActions()
          .filter(action -> !action.getRoute().equals(route))
          .map(RouteAdvertisement::getRoute)
          .forEach(affectedRoutes::add);
      initialDelta
          .getPrefixes()
          .flatMap(p -> getAffectedRoutes(p).stream())
          .forEach(affectedRoutes::add);
      while (!affectedRoutes.isEmpty()) {
        AnnotatedRoute<AbstractRoute> nextRoute = affectedRoutes.iterator().next();
        affectedRoutes.remove(nextRoute);
        delta.from(processAffectedRoute(nextRoute, affectedRoutes));
      }
      return delta.build();
    }

    /**
     * If affected route is resolvable when added, add it. Otherwise remove it. If a net change is
     * made to the RIB and {@code remainingAffectedRoutes} is non-null, then add newly affected
     * routes to {@code remainingAffectedRoutes}. Returns the net change to the RIB.
     */
    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> processAffectedRoute(
        AnnotatedRoute<AbstractRoute> affectedRoute,
        Collection<AnnotatedRoute<AbstractRoute>> remainingAffectedRoutes) {
      RibDelta<AnnotatedRoute<AbstractRoute>> delta;
      boolean isNextHopIpRoute = isNextHopIpRoute(affectedRoute);
      if (isNextHopIpRoute) {
        if (!_routesByNextHopIp
            .get(affectedRoute.getRoute().getNextHopIp())
            .contains(affectedRoute)) {
          // This route was explicitly removed and should not be re-evaluated.
          return RibDelta.empty();
        }
        Set<AnnotatedRoute<AbstractRoute>> initialLpm = lpmIfValid(affectedRoute);
        if (initialLpm.isEmpty()) {
          delta = Rib.super.removeRouteGetDelta(affectedRoute);
          if (delta.getActions().count() > 1) {
            // backup routes replaced the removed route
            delta
                .getActions()
                .filter(action -> !action.isWithdrawn())
                .forEach(action -> remainingAffectedRoutes.add(action.getRoute()));
          }
          postProcessDelta(delta);
        } else {
          // The route has a next hop, but we need to check for a loop.
          delta = Rib.super.mergeRouteGetDelta(affectedRoute);
          postProcessDelta(delta);
          _resolutionGraph.removeAllEdges(
              ImmutableSet.copyOf(_resolutionGraph.outgoingEdgesOf(affectedRoute)));
          // Recompute affected route edges. Should not loop since we haven't
          // Connected out edges of affectedRoute yet.
          updateAffectedRoutesOutEdges(affectedRoute);
          try {
            // Connect out edges of affected route. If it induces a loop, we must remove the route
            // and restore the edges of the its affected routes.
            initialLpm.forEach(lpmRoute -> _resolutionGraph.addEdge(affectedRoute, lpmRoute));
          } catch (IllegalArgumentException e) {
            // A cycle was detected.
            RibDelta<AnnotatedRoute<AbstractRoute>> removal =
                Rib.super.removeRouteGetDelta(affectedRoute);
            assert delta.isEmpty() ^ removal.isEmpty();
            postProcessDelta(removal);
            RibDelta<AnnotatedRoute<AbstractRoute>> netChange =
                RibDelta.<AnnotatedRoute<AbstractRoute>>builder().from(delta).from(removal).build();
            // Either:
            // - The route was already present, the initial delta was empty, the removal is
            //   non-empty, and the net change is non-empty.
            // - The route was added, the initial delta was non-empty, and the removal is also
            //   non-empty. The net change should be empty.
            assert !removal.isEmpty() && (delta.isEmpty() ^ netChange.isEmpty());
            delta = netChange;
            // Repair out edges of affected routes.
            updateAffectedRoutesOutEdges(affectedRoute);
          }
        }
      } else {
        delta = RibDelta.empty();
      }
      if (!isNextHopIpRoute || !delta.isEmpty()) {
        remainingAffectedRoutes.addAll(getAffectedRoutes(affectedRoute.getNetwork()));
      }
      return delta;
    }

    /**
     * Recompute the out edges for all routes affected by {@code route}. As a precondition, {@code
     * route} should not have any out edges. Otherwise, a loop may be created when updating edges.
     */
    private void updateAffectedRoutesOutEdges(AnnotatedRoute<AbstractRoute> route) {
      getAffectedRoutes(route.getNetwork()).stream()
          .filter(_resolutionGraph::containsVertex)
          .forEach(
              activeAffectedRoute -> {
                _resolutionGraph.removeAllEdges(
                    ImmutableSet.copyOf(_resolutionGraph.outgoingEdgesOf(activeAffectedRoute)));
                lpmIfValid(activeAffectedRoute)
                    .forEach(resolver -> _resolutionGraph.addEdge(activeAffectedRoute, resolver));
              });
    }

    /**
     * Get a list of all tracked next hop IP routes affected by an update to a route with the given
     * {@code prefix}.
     */
    private @Nonnull List<AnnotatedRoute<AbstractRoute>> getAffectedRoutes(Prefix prefix) {
      Set<Ip> affectedNextHopIps = _ribResolutionTrie.getAffectedNextHopIps(prefix);
      return affectedNextHopIps.stream()
          .flatMap(nhip -> _routesByNextHopIp.get(nhip).stream())
          .collect(ImmutableList.toImmutableList());
    }
  }

  /**
   * Returns {@code true} iff {@code route} is a next hop IP route whose network contains its own
   * next hop IP.
   */
  private static boolean containsOwnNextHop(AnnotatedRoute<AbstractRoute> route) {
    return isNextHopIpRoute(route)
        && route.getNetwork().containsIp(route.getAbstractRoute().getNextHopIp());
  }

  /**
   * Returns {@code true} {@code route} is a next hop IP only route (excludes routes that also have
   * a next hop interface).
   */
  private static boolean isNextHopIpRoute(AnnotatedRoute<AbstractRoute> route) {
    return route.getAbstractRoute().getNextHop() instanceof NextHopIp;
  }

  /** Create a new empty RIB. */
  public Rib() {
    this(false);
  }

  /**
   * Create a new empty RIB. If {@code enforceResolvability} is {@code true}, then merged routes
   * that are not resolvable will remain inactive until they become resolvable again. Inactive
   * routes will not be returned by {@link #getRoutes()}, {@link #getTypedRoutes()}, nor {@link
   * #getTypedBackupRoutes()}.
   */
  public Rib(boolean enforceResolvability) {
    super(true);
    _resolvabilityEnforcer = enforceResolvability ? new ResolvabilityEnforcer() : null;
  }

  @Override
  public int comparePreference(
      @Nonnull AnnotatedRoute<AbstractRoute> lhs, @Nonnull AnnotatedRoute<AbstractRoute> rhs) {
    // Flipped rhs & lhs because lower values are preferable.
    return Comparator.comparing(AbstractRoute::getAdministrativeCost)
        .thenComparing(AbstractRoute::getMetric)
        .compare(rhs.getRoute(), lhs.getRoute());
  }

  @Override
  @Nonnull
  public RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
      AnnotatedRoute<AbstractRoute> route) {
    return !route.getRoute().getNonRouting()
        ? _resolvabilityEnforcer != null
            ? _resolvabilityEnforcer.mergeRouteGetDelta(route)
            : super.mergeRouteGetDelta(route)
        : RibDelta.empty();
  }

  @Nonnull
  @Override
  public RibDelta<AnnotatedRoute<AbstractRoute>> removeRouteGetDelta(
      AnnotatedRoute<AbstractRoute> route) {
    return !route.getRoute().getNonRouting()
        ? _resolvabilityEnforcer != null
            ? _resolvabilityEnforcer.removeRouteGetDelta(route)
            : super.removeRouteGetDelta(route)
        : RibDelta.empty();
  }

  private final @Nullable ResolvabilityEnforcer _resolvabilityEnforcer;
}
