package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
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
    private final @Nonnull ResolutionRestriction<AnnotatedRoute<AbstractRoute>>
        _resolutionRestriction;
    private final @Nonnull Map<AnnotatedRoute<AbstractRoute>, Boolean> _resolutionRestrictionCache;

    private ResolvabilityEnforcer(
        ResolutionRestriction<AnnotatedRoute<AbstractRoute>> resolutionRestriction) {
      _routesByNextHopIp = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
      _ribResolutionTrie = new RibResolutionTrie();
      _resolutionGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
      _resolutionRestrictionCache = new HashMap<>();
      _resolutionRestriction =
          route ->
              _resolutionRestrictionCache.computeIfAbsent(
                  route, computeResolutionRestriction(resolutionRestriction)::test);
    }

    /** Wrap resolution restriction with some whitelisting if needed. */
    private @Nonnull ResolutionRestriction<AnnotatedRoute<AbstractRoute>>
        computeResolutionRestriction(
            ResolutionRestriction<AnnotatedRoute<AbstractRoute>> baseResolutionRestriction) {
      if (baseResolutionRestriction
          == ResolutionRestriction.<AnnotatedRoute<AbstractRoute>>alwaysTrue()) {
        // no need to whitelist anything, so stop here.
        return baseResolutionRestriction;
      }
      return route -> {
        if (route.getAbstractRoute().getProtocol() == RoutingProtocol.CONNECTED) {
          // Connected routes can always be used to resolve.
          // TODO: confirm generally
          return true;
        }
        return baseResolutionRestriction.test(route);
      };
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      if (isRecursiveNextHopIpRoute(route)) {
        Ip nextHopIp = route.getAbstractRoute().getNextHopIp();
        _routesByNextHopIp.put(nextHopIp, route);
        _ribResolutionTrie.addNextHopIp(nextHopIp);
      }
      return processSideEffects(route, Rib.super.mergeRouteGetDelta(route));
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> removeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      _resolutionRestrictionCache.remove(route);
      if (isRecursiveNextHopIpRoute(route)) {
        Ip nextHopIp = route.getAbstractRoute().getNextHopIp();
        if (_routesByNextHopIp.remove(nextHopIp, route)
            && _routesByNextHopIp.get(nextHopIp).isEmpty()) {
          _ribResolutionTrie.removeNextHopIp(nextHopIp);
        }
      }
      return processSideEffects(route, Rib.super.removeRouteGetDelta(route, Reason.WITHDRAW));
    }

    /**
     * Following a delta resulting from a call to {@link AbstractRib#mergeRouteGetDelta} {@link
     * AbstractRib#removeRouteGetDelta}, propagate the changes to resolution metadata as follows:
     *
     * <ul>
     *   <li>To ensure correctness of affected routes, add or remove the prefix corresponding to the
     *       delta from the {@link #_ribResolutionTrie} if a first route for that network was added
     *       or a last route for that network was removed from the RIB.
     *   <li>To prevent spurious detected loops, remove any removed routes from the {@link
     *       #_resolutionGraph}.
     *   <li>To prepare for loop detection on new routes, add vertices for added routes to the
     *       {@link #_resolutionGraph}. Edges must be added later in a specific order to ensure that
     *       a detected loop results in the correct route(s) being withdrawn.
     * </ul>
     */
    void postProcessDelta(RibDelta<AnnotatedRoute<AbstractRoute>> delta) {
      if (delta.isEmpty()) {
        return;
      }
      Prefix prefix = delta.getPrefixes().findFirst().get();
      assert delta.getPrefixes().allMatch(prefix::equals);
      if (delta.getActions().count() == 1) {
        // No backup replacement, but there may be existing route(s) before an add or after a
        // withdraw. Need to check if anything now present matches resolution restriction.
        if (extractRoutes(prefix).stream().anyMatch(_resolutionRestriction)) {
          _ribResolutionTrie.addPrefix(prefix);
        } else {
          _ribResolutionTrie.removePrefix(prefix);
        }
      } else {
        // More than one action, and this delta is the result of shifting backups.
        // The only available routes now are the ADDs in the delta (either backup routes that were
        // promoted after a removal, or an added route that demoted existing routes to backup). So
        // we don't have to check the RIB.
        if (delta
            .getActions()
            .anyMatch(
                action ->
                    !action.isWithdrawn() && _resolutionRestriction.test(action.getRoute()))) {
          _ribResolutionTrie.addPrefix(prefix);
        } else {
          _ribResolutionTrie.removePrefix(prefix);
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
     * Performs longest prefix match on a next hop IP route. If the route passes the resolution
     * restriction, contains its own next hop IP, and the result does not contain a more specific
     * route, returns the empty set. Otherwise, returns the LPM routes.
     */
    private @Nonnull Set<AnnotatedRoute<AbstractRoute>> lpmIfValid(
        AnnotatedRoute<AbstractRoute> nhipRoute) {
      Set<AnnotatedRoute<AbstractRoute>> lpmRoutes =
          longestPrefixMatch(nhipRoute.getRoute().getNextHopIp(), _resolutionRestriction);
      if (containsOwnNextHop(nhipRoute) && _resolutionRestriction.test(nhipRoute)) {
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
      assert initialDelta.getRoutes().contains(route);
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> delta =
          RibDelta.<AnnotatedRoute<AbstractRoute>>builder().from(initialDelta);
      postProcessDelta(initialDelta);
      Set<AnnotatedRoute<AbstractRoute>> affectedRoutes = new LinkedHashSet<>();
      // make sure route gets added first, then add everything else in the delta
      affectedRoutes.add(route);
      initialDelta.getActions().map(RouteAdvertisement::getRoute).forEach(affectedRoutes::add);
      initialDelta.getPrefixes().flatMap(p -> getAffectedRoutes(p)).forEach(affectedRoutes::add);
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
      if (_backupRoutes.containsEntry(affectedRoute.getNetwork(), affectedRoute)
          && !extractRoutes(affectedRoute.getNetwork()).contains(affectedRoute)) {
        // The affected route is currently in backup. It cannot be activated nor deactivated until
        // all better routes have been removed/deactivated. It has already been removed from the
        // resolution graph, and its affected routes should have already been queued when a better
        // route was activated.
        assert !_resolutionGraph.containsVertex(affectedRoute);
        return RibDelta.empty();
      }
      RibDelta<AnnotatedRoute<AbstractRoute>> delta;
      // TODO: Change to checking for just isNextHopIpRoute when this class handles (de)activation
      //       of nonrecursive static routes instead of relying on StaticRouteHelper in subsequent
      //       iteration.
      boolean isRecursiveNextHopIpRoute = isRecursiveNextHopIpRoute(affectedRoute);
      if (isRecursiveNextHopIpRoute) {
        if (!_routesByNextHopIp
            .get(affectedRoute.getRoute().getNextHopIp())
            .contains(affectedRoute)) {
          // The affected route was explicitly removed by a client remove call. Such a route cannot
          // be re-activated, has already been removed from the resolution graph, and its affected
          // routes should already have been queued.
          assert !_resolutionGraph.containsVertex(affectedRoute);
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
          delta = performLoopDetection(affectedRoute, delta, initialLpm);
        }
      } else {
        delta = RibDelta.empty();
      }
      if (_resolutionRestriction.test(affectedRoute)
          && (!isRecursiveNextHopIpRoute || !delta.isEmpty())) {
        getAffectedRoutes(affectedRoute.getNetwork()).forEach(remainingAffectedRoutes::add);
      }
      return delta;
    }

    /**
     * Perform loop detection on the {@code affectedRoute} given the resulting post-processed {@code
     * mergeDelta} and {@code lpmRoutes} for it. If a loop is detected, withdraw the route. Return
     * the final delta after potentially withdrawing the route.
     */
    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> performLoopDetection(
        AnnotatedRoute<AbstractRoute> affectedRoute,
        RibDelta<AnnotatedRoute<AbstractRoute>> mergeDelta,
        Set<AnnotatedRoute<AbstractRoute>> lpmRoutes) {
      if (!isRecursiveNextHopIpRoute(affectedRoute)
          || !_resolutionRestriction.test(affectedRoute)) {
        // Non-recursive routes cannot loop.
        // Routes unusable for resolution cannot loop.
        return mergeDelta;
      }
      RibDelta<AnnotatedRoute<AbstractRoute>> finalDelta = mergeDelta;
      _resolutionGraph.removeAllEdges(
          ImmutableSet.copyOf(_resolutionGraph.outgoingEdgesOf(affectedRoute)));
      // Recompute affected route edges. Should not loop since we haven't
      // Connected out edges of affectedRoute yet.
      updateAffectedRoutesOutEdges(affectedRoute);
      try {
        // Connect out edges of affected route. If it induces a loop, we must remove the route
        // and restore the edges of the its affected routes.
        lpmRoutes.forEach(lpmRoute -> _resolutionGraph.addEdge(affectedRoute, lpmRoute));
      } catch (IllegalArgumentException e) {
        // A cycle was detected.
        RibDelta<AnnotatedRoute<AbstractRoute>> removal =
            Rib.super.removeRouteGetDelta(affectedRoute);
        postProcessDelta(removal);
        RibDelta<AnnotatedRoute<AbstractRoute>> netChange =
            RibDelta.<AnnotatedRoute<AbstractRoute>>builder()
                .from(mergeDelta)
                .from(removal)
                .build();
        // Either:
        // - The route was already present, the initial delta was empty, the removal is
        //   non-empty, and the net change is non-empty.
        // - The route was added, the initial delta was non-empty, and the removal is also
        //   non-empty. The net change should be empty.
        assert !removal.isEmpty() && (mergeDelta.isEmpty() ^ netChange.isEmpty());
        finalDelta = netChange;
        // Repair out edges of affected routes.
        updateAffectedRoutesOutEdges(affectedRoute);
      }
      return finalDelta;
    }

    /**
     * Recompute the out edges for all routes affected by {@code route}. As a precondition, {@code
     * route} should not have any out edges. Otherwise, a loop may be created when updating edges.
     */
    private void updateAffectedRoutesOutEdges(AnnotatedRoute<AbstractRoute> route) {
      getAffectedRoutes(route.getNetwork())
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
     * Get a stream of all tracked next hop IP routes affected by an update to a route with the
     * given {@code prefix}.
     */
    private @Nonnull Stream<AnnotatedRoute<AbstractRoute>> getAffectedRoutes(Prefix prefix) {
      Set<Ip> affectedNextHopIps = _ribResolutionTrie.getAffectedNextHopIps(prefix);
      return affectedNextHopIps.stream().flatMap(nhip -> _routesByNextHopIp.get(nhip).stream());
    }
  }

  /**
   * Returns {@code true} iff {@code route} is a recursive next hop IP route whose network contains
   * its own next hop IP.
   */
  private static boolean containsOwnNextHop(AnnotatedRoute<AbstractRoute> route) {
    return isRecursiveNextHopIpRoute(route)
        && route.getNetwork().containsIp(route.getAbstractRoute().getNextHopIp());
  }

  /**
   * Returns {@code true} {@code route} is a next hop IP only route (excludes routes that also have
   * a next hop interface).
   */
  private static boolean isNextHopIpRoute(AnnotatedRoute<AbstractRoute> route) {
    return route.getAbstractRoute().getNextHop() instanceof NextHopIp;
  }

  /**
   * Returns {@code true} {@code route} is a recursive next hop IP only route (excludes routes that
   * also have a next hop interface, and excludes non-recursive static routes).
   */
  private static boolean isRecursiveNextHopIpRoute(AnnotatedRoute<AbstractRoute> route) {
    AbstractRoute abstractRoute = route.getAbstractRoute();
    return isNextHopIpRoute(route)
        && (!(abstractRoute instanceof StaticRoute)
            || ((StaticRoute) abstractRoute).getRecursive());
  }

  /** Create a new empty RIB. Resolvability of recursive routes will not be enforced. */
  public Rib() {
    this(null);
  }

  /**
   * Create a new empty RIB. If {@code resolutionRestriction} is not {@code null}, then merged
   * recursive routes that are not resolvable under the provided {@code resolutionRestriction} will
   * remain inactive until they become resolvable again. Inactive routes will not be returned by
   * {@link #getRoutes()}, {@link #getTypedRoutes()}, nor {@link #getTypedBackupRoutes()}.
   */
  public Rib(@Nullable ResolutionRestriction<AnnotatedRoute<AbstractRoute>> resolutionRestriction) {
    super(true);
    _resolvabilityEnforcer =
        resolutionRestriction != null ? new ResolvabilityEnforcer(resolutionRestriction) : null;
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

  private final transient @Nullable ResolvabilityEnforcer _resolvabilityEnforcer;
}
