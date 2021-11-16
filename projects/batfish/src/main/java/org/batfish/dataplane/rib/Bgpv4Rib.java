package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.GenericRibReadOnly;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;

/** BGPv4-specific RIB implementation */
@ParametersAreNonnullByDefault
public final class Bgpv4Rib extends BgpRib<Bgpv4Route> {
  private final class ResolvabilityEnforcer {
    /** Map of NHIP to {@link Bgpv4Route} with that NHIP */
    private final @Nonnull SetMultimap<Ip, Bgpv4Route> _bgpRoutesByNhip;

    private final @Nonnull RibResolutionTrie _mainRibPrefixesAndBgpNhips;

    private ResolvabilityEnforcer() {
      _bgpRoutesByNhip = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
      _mainRibPrefixesAndBgpNhips = new RibResolutionTrie();
    }

    void addBgpRoute(Bgpv4Route route) {
      Ip nhip = route.getNextHopIp();
      _bgpRoutesByNhip.put(nhip, route);
      _mainRibPrefixesAndBgpNhips.addNextHopIp(nhip);
    }

    void removeBgpRoute(Bgpv4Route route) {
      Ip nhip = route.getNextHopIp();
      _bgpRoutesByNhip.remove(nhip, route);
      if (!_bgpRoutesByNhip.containsKey(nhip)) {
        _mainRibPrefixesAndBgpNhips.removeNextHopIp(nhip);
      }
    }

    @Nonnull
    Stream<Ip> getAffectedNextHopIps(Stream<Prefix> changedPrefixes) {
      return changedPrefixes
          .flatMap(prefix -> _mainRibPrefixesAndBgpNhips.getAffectedNextHopIps(prefix).stream())
          .distinct();
    }

    @Nonnull
    Set<Bgpv4Route> getRoutesWithNhip(Ip nhip) {
      return ImmutableSet.copyOf(_bgpRoutesByNhip.get(nhip));
    }

    void updateMainRibPrefixes(RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta) {
      mainRibDelta
          .getActions()
          // TODO Filter to routes that pass the resolution restriction, when one is added
          .forEach(
              action -> {
                Prefix prefix = action.getRoute().getNetwork();
                if (!action.isWithdrawn()) {
                  // Route was added (note that the same route can't be removed in the same delta)
                  _mainRibPrefixesAndBgpNhips.addPrefix(prefix);
                } else if (_mainRib.getRoutes(prefix).isEmpty()) {
                  // Route was removed, and there are no remaining routes for this prefix
                  _mainRibPrefixesAndBgpNhips.removePrefix(prefix);
                }
              });
    }
  }

  private final @Nonnull ResolvabilityEnforcer _resolvabilityEnforcer;
  private final @Nonnull Map<OriginMechanism, SortedSetMultimap<Prefix, Bgpv4Route>> _localRoutes;
  private final @Nonnull Map<OriginMechanism, Comparator<Bgpv4Route>> _localRouteComparators;

  public Bgpv4Rib(
      @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean clusterListAsIgpCost,
      LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker,
      NextHopIpTieBreaker networkNextHopIpTieBreaker,
      NextHopIpTieBreaker redistributeNextHopIpTieBreaker) {
    super(
        mainRib,
        tieBreaker,
        maxPaths,
        multipathEquivalentAsPathMatchMode,
        true,
        clusterListAsIgpCost,
        localOriginationTypeTieBreaker);
    _resolvabilityEnforcer = new ResolvabilityEnforcer();
    _localRouteComparators =
        initLocalRouteComparators(networkNextHopIpTieBreaker, redistributeNextHopIpTieBreaker);
    _localRoutes = new EnumMap<>(OriginMechanism.class);
    checkArgument(localOriginationTypeTieBreaker != null, "crap");
    checkArgument(networkNextHopIpTieBreaker != null, "crap");
    checkArgument(redistributeNextHopIpTieBreaker != null, "crap");
  }

  private static @Nonnull Map<OriginMechanism, Comparator<Bgpv4Route>> initLocalRouteComparators(
      NextHopIpTieBreaker networkNextHopIpTieBreaker,
      NextHopIpTieBreaker redistributeNextHopIpTieBreaker) {
    Map<OriginMechanism, Comparator<Bgpv4Route>> map = new EnumMap<>(OriginMechanism.class);
    map.put(OriginMechanism.NETWORK, toLocalRouteComparator(networkNextHopIpTieBreaker));
    map.put(OriginMechanism.REDISTRIBUTE, toLocalRouteComparator(redistributeNextHopIpTieBreaker));
    return map;
  }

  private static @Nonnull Comparator<Bgpv4Route> toLocalRouteComparator(
      NextHopIpTieBreaker nextHopIpTieBreaker) {
    Comparator<Ip> nhipComparator =
        nextHopIpTieBreaker == HIGHEST_NEXT_HOP_IP ? Comparator.reverseOrder() : Ip::compareTo;
    return Comparator.comparing(Bgpv4Route::getNextHopIp, nhipComparator)
        .thenComparing(Bgpv4Route::getSrcProtocol);
  }

  @Nonnull
  @Override
  public RibDelta<Bgpv4Route> mergeRouteGetDelta(Bgpv4Route route) {
    /*
      Do not merge routes for which next hop is not reachable.
      However, due to some complications with how we create routes, we must skip this check for:
      - routes with link-local address as next hop (i.e., next-hop interface is set to something)
      - routes with protocol AGGREGATE (for locally-generated routes/aggregates)
      - routes that have a next vrf as the next hop
    */
    if (shouldCheckNextHopReachability(route) && _mainRib != null) {
      _resolvabilityEnforcer.addBgpRoute(route);
      if (!isResolvable(route.getNextHopIp())) {
        return RibDelta.empty();
      }
    } else if (route.isTrackableLocalRoute()) {
      // TODO: more correct to filter main rib routes prior to converting to BGP
      return addLocalRoute(route);
    }
    return super.mergeRouteGetDelta(route);
  }

  private boolean isBestLocalRoute(Bgpv4Route route) {
    OriginMechanism o = route.getOriginMechanism();
    assert _localRoutes.containsKey(o);
    SortedSet<Bgpv4Route> routesForPrefix = _localRoutes.get(o).get(route.getNetwork());
    assert !routesForPrefix.isEmpty();
    return route.equals(routesForPrefix.first());
  }

  /**
   * Add tracking for a local (non-learned) route. Then add it to the RIB, withdrawing any non-best
   * local routes for the same origin mechanism and prefix.
   */
  private @Nonnull RibDelta<Bgpv4Route> addLocalRoute(Bgpv4Route route) {
    Prefix prefix = route.getNetwork();
    SortedSet<Bgpv4Route> routesForMechanismAndPrefix =
        _localRoutes
            .computeIfAbsent(
                route.getOriginMechanism(),
                o ->
                    Multimaps.newSortedSetMultimap(
                        new HashMap<>(),
                        () ->
                            new TreeSet<>(_localRouteComparators.get(route.getOriginMechanism()))))
            .get(prefix);
    Optional<Bgpv4Route> maybeOldBest =
        routesForMechanismAndPrefix.isEmpty()
            ? Optional.empty()
            : Optional.of(routesForMechanismAndPrefix.first());
    routesForMechanismAndPrefix.add(route);
    Bgpv4Route newBest = routesForMechanismAndPrefix.first();
    if (!route.equals(newBest)
        || (maybeOldBest.isPresent() && maybeOldBest.get().equals(newBest))) {
      return RibDelta.empty();
    }
    // route is new best, so needs to be added
    RibDelta.Builder<Bgpv4Route> delta =
        RibDelta.<Bgpv4Route>builder().from(super.mergeRouteGetDelta(route));
    // if old best was present, remove it
    maybeOldBest.ifPresent(oldBest -> delta.from(super.removeRouteGetDelta(oldBest)));
    return delta.build();
  }

  @Nonnull
  @Override
  public RibDelta<Bgpv4Route> removeRouteGetDelta(Bgpv4Route route) {
    if (route.isTrackableLocalRoute()) {
      return removeLocalRoute(route);
    }
    // Remove route from resolvability enforcer so it can't get reactivated.
    // No effect if the main RIB is null or if the route doesn't need resolving.
    _resolvabilityEnforcer.removeBgpRoute(route);
    return super.removeRouteGetDelta(route);
  }

  private @Nonnull RibDelta<Bgpv4Route> removeLocalRoute(Bgpv4Route route) {
    // TODO: In the case that two main RIB routes get redistributed to the same route here,
    //       withdrawal of one will improperly cause withdrawal of the BGP route. This is actually
    //       a problem for all iBDP redistribution (and even add-path) that needs to be addressed
    //       carefully.
    OriginMechanism o = route.getOriginMechanism();
    if (!_localRoutes.containsKey(o)) {
      // no routes of this mechanism, so nothing to do
      return RibDelta.empty();
    }
    SortedSet<Bgpv4Route> routesForMechanismAndPrefix = _localRoutes.get(o).get(route.getNetwork());
    // stop tracking this route
    routesForMechanismAndPrefix.remove(route);
    RibDelta.Builder<Bgpv4Route> delta = RibDelta.builder();
    if (!routesForMechanismAndPrefix.isEmpty()) {
      // add the new best. no effect if already present.
      delta.from(super.mergeRouteGetDelta(routesForMechanismAndPrefix.first()));
    }
    // remove the route. no effect if already absent.
    delta.from(super.removeRouteGetDelta(route));
    return delta.build();
  }

  public MultipathRibDelta<Bgpv4Route> updateActiveRoutes(
      RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta) {
    // Should only be null in tests, and those tests shouldn't be using this function
    assert _mainRib != null;

    // Update resolvability enforcer's record of main RIB prefixes
    _resolvabilityEnforcer.updateMainRibPrefixes(mainRibDelta);

    // (De)activate BGP routes based on updated main RIB
    RibDelta.Builder<Bgpv4Route> bestPathDelta = RibDelta.builder();
    RibDelta.Builder<Bgpv4Route> multipathDelta = RibDelta.builder();
    _resolvabilityEnforcer
        .getAffectedNextHopIps(mainRibDelta.getPrefixes())
        .forEach(
            nhip -> {
              boolean resolvable = isResolvable(nhip);
              for (Bgpv4Route affectedRoute : _resolvabilityEnforcer.getRoutesWithNhip(nhip)) {
                MultipathRibDelta<Bgpv4Route> delta =
                    resolvable
                        ? super.multipathMergeRouteGetDelta(affectedRoute)
                        : super.multipathRemoveRouteGetDelta(affectedRoute);
                bestPathDelta.from(delta.getBestPathDelta());
                multipathDelta.from(delta.getMultipathDelta());
              }
            });
    return new MultipathRibDelta<>(bestPathDelta.build(), multipathDelta.build());
  }

  /**
   * Returns whether the given next hop IP is resolvable in the main RIB. Assumes that main RIB is
   * not {@code null}.
   */
  private boolean isResolvable(Ip nhip) {
    assert _mainRib != null;
    // TODO: implement resolution restriction
    return !_mainRib.longestPrefixMatch(nhip, alwaysTrue()).isEmpty();
  }

  /**
   * Returns whether to check reachability (i.e., look for a route in main RIB that resolves this
   * route's next hop IP), if applicable.
   */
  private static boolean shouldCheckNextHopReachability(Bgpv4Route route) {
    if (route.getProtocol() == RoutingProtocol.AGGREGATE || route.isTrackableLocalRoute()) {
      return false;
    }
    return NEXT_HOP_REACHABILITY_VISITOR.visit(route.getNextHop());
  }

  private static final NextHopVisitor<Boolean> NEXT_HOP_REACHABILITY_VISITOR =
      new NextHopVisitor<Boolean>() {
        @Override
        public Boolean visitNextHopIp(NextHopIp nextHopIp) {
          // Real IP, we should make sure we can reach it.
          return true;
        }

        @Override
        public Boolean visitNextHopInterface(NextHopInterface nextHopInterface) {
          // Next hop-interface. Likely BGP unnumbered, nothing to check
          return false;
        }

        @Override
        public Boolean visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          // Discard route, nothing to check
          return false;
        }

        @Override
        public Boolean visitNextHopVrf(NextHopVrf nextHopVrf) {
          // The next hop is in a different VRF, so nothing to check in current main RIB
          return false;
        }
      };
}
