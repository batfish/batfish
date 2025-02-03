package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.OriginMechanism.NETWORK;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;
import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_NETWORK;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_REDISTRIBUTE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.GenericRibReadOnly;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.ReceivedFromInterface;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.datamodel.visitors.LegacyReceivedFromToIpConverter;
import org.batfish.datamodel.visitors.ReceivedFromVisitor;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * A generic BGP RIB containing the common properties among the RIBs for different types of BGP
 * routes
 */
@ParametersAreNonnullByDefault
public abstract class BgpRib<R extends BgpRoute<?, ?>> extends AbstractRib<R> {

  private static final int MAX_RESOLUTION_DEPTH = 10;

  /** Main RIB to use for IGP cost estimation and next hop resolution */
  protected final @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> _mainRib;

  /** Tie breaker to use if all route attributes appear to be equal */
  protected final @Nonnull BgpTieBreaker _tieBreaker;

  /** Maximum number of paths to install. Unconstrained (infinite) if {@code null} */
  protected final @Nullable Integer _maxPaths;

  /**
   * For multipath: how strict should the comparison of AS Path be for paths to be considered equal
   */
  protected final @Nullable MultipathEquivalentAsPathMatchMode _multipathEquivalentAsPathMatchMode;

  // Best BGP paths. Invariant: must be re-evaluated (per prefix) each time a route is added or
  // evicted
  protected final @Nonnull Map<Prefix, R> _bestPaths;

  /**
   * This logical clock helps us keep track when routes were merged into the RIB to determine their
   * age. It's incremented each time a route is merged into the RIB.
   */
  protected long _logicalClock;

  /** Map to keep track when routes were merged in. */
  protected Map<R, Long> _logicalArrivalTime;

  /** For FRR, Cluster List Length is used as an IGP metric. */
  protected boolean _clusterListAsIgpCost;

  /**
   * Tie-breaking mode for local routes. Only relevant on devices that use an independent network
   * policy.
   */
  private final @Nonnull LocalOriginationTypeTieBreaker _localOriginationTypeTieBreaker;

  protected BgpRib(
      @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean withBackups,
      boolean clusterListAsIgpCost,
      LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker) {
    super(withBackups);
    _mainRib = mainRib;
    _tieBreaker = tieBreaker;
    _clusterListAsIgpCost = clusterListAsIgpCost;
    checkArgument(maxPaths == null || maxPaths > 0, "Invalid max-paths value %s", maxPaths);
    if (maxPaths != null && maxPaths > 1) {
      /*
       * Essentially make this infinite. This is a design choice to enable more comprehensive path
       * analysis up the stack (e.g., reachability, multipath consistency, etc.)
       */
      _maxPaths = null;
    } else {
      _maxPaths = maxPaths;
    }
    checkArgument(
        Integer.valueOf(1).equals(maxPaths) || multipathEquivalentAsPathMatchMode != null,
        "Multipath AS-Path-Match-mode must be specified for a multipath BGP RIB");
    _multipathEquivalentAsPathMatchMode = multipathEquivalentAsPathMatchMode;
    _bestPaths = new HashMap<>(0);
    _logicalArrivalTime = new HashMap<>(0);
    _logicalClock = 0;
    _localOriginationTypeTieBreaker = localOriginationTypeTieBreaker;
  }

  /*
   * Resources for understanding this logic and the missing features:
   *
   * - https://www.cisco.com/c/en/us/support/docs/ip/border-gateway-protocol-bgp/13753-25.html
   * - https://www.juniper.net/documentation/en_US/junos/topics/reference/general/routing-protocols-address-representation.html
   * - http://docs.frrouting.org/en/latest/bgp.html
   */
  @Override
  public int comparePreference(R lhs, R rhs) {
    // TODO: this step might need to go in a different position
    if (_localOriginationTypeTieBreaker != NO_PREFERENCE
        && lhs.getOriginMechanism() != rhs.getOriginMechanism()
        && lhs.isTrackableLocalRoute()
        && rhs.isTrackableLocalRoute()) {
      // comparing local routes with different origin mechanisms, and one mechanism is preferred
      // over the other.
      if (_localOriginationTypeTieBreaker == PREFER_NETWORK) {
        return lhs.getOriginMechanism() == NETWORK ? 1 : -1;
      } else {
        assert _localOriginationTypeTieBreaker == PREFER_REDISTRIBUTE;
        return lhs.getOriginMechanism() == REDISTRIBUTE ? 1 : -1;
      }
    }
    int multipathCompare =
        Comparator
            // Prefer higher Weight (cisco only)
            .comparing(R::getWeight)
            // Prefer higher LocalPref
            .thenComparing(R::getLocalPreference)
            // NOTE: Accumulated interior gateway protocol (AIGP) is not supported
            // Aggregates (for non-juniper devices, won't appear on Juniper)
            .thenComparing(r -> getAggregatePreference(r.getProtocol()))
            // AS path: prefer shorter
            // TODO: support `bestpath as-path ignore` (both cisco, juniper)
            .thenComparing(r -> r.getAsPath().length(), Comparator.reverseOrder())
            // Prefer certain origin type Internal over External over Incomplete
            .thenComparing(r -> r.getOriginType().getPreference())
            // Prefer eBGP over iBGP
            .thenComparing(r -> getTypeCost(r.getProtocol()))
            /*
             * Prefer lower Multi-Exit Discriminator (MED)
             * TODO: better support for MED rules
             * Most rules are currently not supported:
             *    - normally this comparison is done only if the first AS is the same in both AS Paths
             *    - `always-compare-med` -- overrides above
             *    - there are additional confederation rules
             *    - On Juniper `path-selection cisco-nondeterministic` changes behavior
             *    - On Cisco `bgp bestpath med missing-as-worst` changes missing MED values from 0 to MAX_INT
             */
            .thenComparing(R::getMetric, Comparator.reverseOrder())
            // Prefer next hop IPs with the lowest IGP metric
            .thenComparing(this::getIgpCostToNextHopIp, Comparator.reverseOrder())
            // Prefer lowest CLL as a proxy for IGP Metric. FRR-Only.
            .thenComparing(this::getClusterListLength, Comparator.reverseOrder())
            // Evaluate AS path compatibility for multipath
            .thenComparing(this::compareRouteAsPath)
            .compare(lhs, rhs);
    if (multipathCompare != 0 || isMultipath()) {
      return multipathCompare;
    } else {
      return this.bestPathComparator(lhs, rhs);
    }
  }

  @Override
  public @Nonnull RibDelta<R> mergeRouteGetDelta(R route) {
    // Evict older non-trackable-local routes for same prefix, receivedFrom, and path-id.
    // Note that trackable local routes are managed elsewhere,
    // e.g. in Bgpv4Rib.{add,remove}LocalRoute
    RibDelta<R> evictionDelta =
        route.isTrackableLocalRoute() ? RibDelta.empty() : evictSamePrefixReceivedFromPathId(route);

    RibDelta<R> delta = actionRouteGetDelta(route, super::mergeRouteGetDelta);
    if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER) {
      _logicalArrivalTime.put(route, _logicalClock);
      _logicalClock++;
    }
    if (!delta.isEmpty()) {
      delta.getPrefixes().forEach(this::selectBestPath);
    }
    return RibDelta.<R>builder().from(evictionDelta).from(delta).build();
  }

  /**
   * Evict any distinct existing route with the same values as {@code route} for {@link
   * BgpRoute#getNetwork()}, {@link BgpRoute#getReceivedFrom()}, and {@link BgpRoute#getPathId()}.
   */
  private @Nonnull RibDelta<R> evictSamePrefixReceivedFromPathId(R route) {
    Prefix prefix = route.getNetwork();
    R oldRoute =
        getRouteSamePrefixReceivedFromPathId(
            route, _backupRoutes != null ? _backupRoutes.get(prefix) : super.getRoutes(prefix));
    if (oldRoute == null || route.equals(oldRoute)) {
      return RibDelta.empty();
    } else {
      return removeRouteGetDelta(oldRoute);
    }
  }

  /**
   * Return a BGP route with the same values as {@code route} for {@link BgpRoute#getNetwork()},
   * {@link BgpRoute#getReceivedFrom()}, and {@link BgpRoute#getPathId()} from {@code oldRoutes}.
   *
   * <p>If no such route is found in {@code oldRoutes}, return {@code null}.
   */
  protected final @Nullable R getRouteSamePrefixReceivedFromPathId(R route, Iterable<R> oldRoutes) {
    for (R oldRoute : oldRoutes) {
      if (route.getReceivedFrom().equals(oldRoute.getReceivedFrom())
          && Objects.equals(route.getPathId(), oldRoute.getPathId())) {
        return oldRoute;
      }
    }
    return null;
  }

  @Override
  public @Nonnull Set<R> getRoutes(Prefix prefix) {
    return ImmutableSet.copyOf(
        super.getRoutes(prefix).stream()
            .collect(
                Collectors.toMap(
                    BgpRoute::getNextHop,
                    Function.identity(),
                    BinaryOperator.maxBy(this::bestPathComparator)))
            .values());
  }

  /**
   * Given an initial delta resulting from applying superclass merge/remove that ignores the
   * requirement for unique next hops among ECMP routes, post-process that delta into one that
   * respects the unique next-hop constraint.
   *
   * @param initialDelta Superclass RibDelta resulting from merging or removing a route
   * @param beforeRoutes All routes for the prefix of the initially merged/removed route that were
   *     ECMP-best (ignoring next-hop) prior to the initial action.
   */
  private @Nonnull RibDelta<R> uniqueNextHopPostProcessDelta(
      RibDelta<R> initialDelta, Set<R> beforeRoutes) {
    RibDelta.Builder<R> builder = RibDelta.builder();
    Map<NextHop, SortedSet<R>> bestByNh = new HashMap<>(); // lazily computed
    for (RouteAdvertisement<R> action : initialDelta.getActions()) {
      R route = action.getRoute();
      SortedSet<R> routesForNh =
          bestByNh.computeIfAbsent(
              route.getNextHop(),
              nh -> {
                SortedSet<R> beforeWithNh = new TreeSet<>(this::bestPathComparator);
                for (R r : beforeRoutes) {
                  if (r.getNextHop().equals(nh)) {
                    beforeWithNh.add(r);
                  }
                }
                return beforeWithNh;
              });

      if (action.isWithdrawn()) {
        // withdraw
        assert routesForNh.contains(route);
        R oldBest = routesForNh.last();
        routesForNh.remove(route);
        if (oldBest.equals(route)) {
          builder.from(action);
          if (!routesForNh.isEmpty()) {
            // the removed route was best, so promote the new best
            builder.add(routesForNh.last());
          } // else the removed route was the last one, so the removal is the only action
        } // else another route was better, so no change occurred
      } else {
        // add
        assert !routesForNh.contains(route);
        if (routesForNh.isEmpty()) {
          routesForNh.add(route);
          builder.from(action);
          continue;
        }
        R oldBest = routesForNh.last();
        routesForNh.add(route);
        R newBest = routesForNh.last();
        if (newBest.equals(route)) {
          builder.remove(oldBest, Reason.REPLACE).from(action);
        } // else not the best, so do nothing with this action
      }
    }
    return builder.build();
  }

  /**
   * Compute the multipath-aware delta for taking the given action (add/remove) on the given route.
   */
  private RibDelta<R> actionRouteGetDelta(R route, Function<R, RibDelta<R>> actionFn) {
    if (isMultipath()) {
      Set<R> beforeRoutes = super.getRoutes(route.getNetwork());
      RibDelta<R> delta = actionFn.apply(route);
      return uniqueNextHopPostProcessDelta(delta, beforeRoutes);
    }
    return actionFn.apply(route);
  }

  @Override
  public @Nonnull RibDelta<R> removeRouteGetDelta(R route) {
    RibDelta<R> delta = actionRouteGetDelta(route, super::removeRouteGetDelta);
    if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER) {
      _logicalArrivalTime.remove(route);
    }
    if (!delta.isEmpty()) {
      delta.getPrefixes().forEach(this::selectBestPath);
      if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER) {
        for (RouteAdvertisement<R> a : delta.getActions()) {
          if (a.isWithdrawn()) {
            _logicalArrivalTime.remove(a.getRoute());
          }
        }
      }
    }
    return delta;
  }

  /** Represents the result of a route being added or removed in a potentially multipath BGP RIB. */
  public static class MultipathRibDelta<T extends BgpRoute<?, ?>> {
    private final @Nonnull RibDelta<T> _bestPathDelta;
    private final @Nonnull RibDelta<T> _multipathDelta;

    public MultipathRibDelta(RibDelta<T> bestPathDelta, RibDelta<T> multipathDelta) {
      _bestPathDelta = bestPathDelta;
      _multipathDelta = multipathDelta;
    }

    public @Nonnull RibDelta<T> getBestPathDelta() {
      return _bestPathDelta;
    }

    public @Nonnull RibDelta<T> getMultipathDelta() {
      return _multipathDelta;
    }
  }

  /** Returns a {@link MultipathRibDelta} reflecting the merge of the given {@code route} */
  public @Nonnull MultipathRibDelta<R> multipathMergeRouteGetDelta(R route) {
    if (!isMultipath()) {
      RibDelta<R> multipathDelta = mergeRouteGetDelta(route);
      return new MultipathRibDelta<>(multipathDelta, multipathDelta);
    }
    R currentBestPathRoute = _bestPaths.get(route.getNetwork());
    RibDelta<R> multipathDelta = mergeRouteGetDelta(route);
    if (currentBestPathRoute == null) {
      // Since there was previously no best path, both deltas should be equal
      return new MultipathRibDelta<>(multipathDelta, multipathDelta);
    }
    // Check if best path changed
    R newBestPathRoute = _bestPaths.get(route.getNetwork());
    if (currentBestPathRoute.equals(newBestPathRoute)) {
      // The best path route did not change, so best path delta should be empty
      return new MultipathRibDelta<>(RibDelta.empty(), multipathDelta);
    }
    // This route replaced the old best path route; best path delta should reflect this
    RibDelta<R> bestPathDelta =
        RibDelta.<R>builder()
            .remove(currentBestPathRoute, RouteAdvertisement.Reason.REPLACE)
            .add(route)
            .build();
    return new MultipathRibDelta<>(bestPathDelta, multipathDelta);
  }

  /** Returns a {@link MultipathRibDelta} reflecting the removal of the given {@code route} */
  public @Nonnull MultipathRibDelta<R> multipathRemoveRouteGetDelta(R route) {
    if (!isMultipath()) {
      RibDelta<R> multipathDelta = removeRouteGetDelta(route);
      return new MultipathRibDelta<>(multipathDelta, multipathDelta);
    }
    if (!route.equals(_bestPaths.get(route.getNetwork()))) {
      // Since this route is not the best path, the best path delta should be empty
      return new MultipathRibDelta<>(RibDelta.empty(), removeRouteGetDelta(route));
    }
    // This route is the best path. If there is a new best path, add it to best path delta.
    RibDelta<R> multipathDelta = removeRouteGetDelta(route);
    RibDelta.Builder<R> bestPathDeltaBuilder =
        RibDelta.<R>builder().remove(route, RouteAdvertisement.Reason.WITHDRAW);
    Optional.ofNullable(_bestPaths.get(route.getNetwork())).ifPresent(bestPathDeltaBuilder::add);
    return new MultipathRibDelta<>(bestPathDeltaBuilder.build(), multipathDelta);
  }

  @Override
  protected @Nonnull Set<R> computeRoutes() {
    if (!isMultipath()) {
      return getBestPathRoutes();
    }
    Map<NextHop, Map<Prefix, R>> routesByNhAndPrefix = new HashMap<>();
    for (R route : super.computeRoutes()) {
      routesByNhAndPrefix
          .computeIfAbsent(route.getNextHop(), n -> new HashMap<>())
          .compute(
              route.getNetwork(),
              (p, other) ->
                  other == null ? route : Comparators.max(other, route, this::bestPathComparator));
    }
    return routesByNhAndPrefix.values().stream()
        .flatMap(m -> m.values().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  public int getNumBestPathRoutes() {
    return _bestPaths.size();
  }

  public Set<R> getBestPathRoutes() {
    return ImmutableSet.copyOf(_bestPaths.values());
  }

  private int compareRouteAsPath(R lhs, R rhs) {
    return compareAsPath(lhs.getAsPath(), rhs.getAsPath());
  }

  private int compareAsPath(AsPath lhs, AsPath rhs) {
    if (_multipathEquivalentAsPathMatchMode == null) {
      // Nothing to do; defer to best-path selection
      return 0;
    }
    return switch (_multipathEquivalentAsPathMatchMode) {
      case EXACT_PATH -> lhs.equals(rhs) ? 0 : -1;
      case FIRST_AS -> {
        AsSet lhsFirstAsSet = lhs.getAsSets().isEmpty() ? AsSet.empty() : lhs.getAsSets().get(0);
        AsSet rhsFirstAsSet = rhs.getAsSets().isEmpty() ? AsSet.empty() : rhs.getAsSets().get(0);
        yield lhsFirstAsSet.equals(rhsFirstAsSet) ? 0 : -1;
      }
      case PATH_LENGTH -> {
        assert lhs.length() == rhs.length();
        yield 0;
      }
    };
  }

  private void selectBestPath(Prefix prefix) {
    // optimization - avoid extra computation from override of getRoutes(prefix) in this class
    Set<R> remainingBestPaths = super.getRoutes(prefix);
    if (remainingBestPaths.isEmpty()) {
      // Remove best path and return
      _bestPaths.remove(prefix);
      return;
    }
    R best = Collections.max(remainingBestPaths, this::bestPathComparator);
    _bestPaths.put(prefix, best);
  }

  /**
   * Compare two routes for best path selection. Assumes that given routes have already been checked
   * for multipath equivalence, and thus have the same protocol (e/iBGP, same as path length, etc)
   */
  @VisibleForTesting
  int bestPathComparator(R lhs, R rhs) {
    // Skip arrival order unless requested, only applies if both routes are eBGP.
    if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER
        && lhs.getProtocol() == RoutingProtocol.BGP
        && rhs.getProtocol() == RoutingProtocol.BGP) {
      int result =
          Comparator.<R, Long>comparing(
                  r -> _logicalArrivalTime.getOrDefault(r, _logicalClock),
                  Comparator.reverseOrder())
              .compare(lhs, rhs);
      if (result != 0) {
        return result;
      }
    }

    // Continue with remaining tie breakers
    return
    // Prefer lower originator router ID
    Comparator.comparing(R::getOriginatorIp, Comparator.reverseOrder())
        // Prefer lower cluster list length. Only applicable to iBGP
        .thenComparing(r -> r.getClusterList().size(), Comparator.reverseOrder())
        .thenComparing(R::getReceivedFrom, BgpRib::compareReceivedFrom)
        // Prefer no path ID, then lower path ID
        .thenComparing(R::getPathId, Comparator.nullsLast(Comparator.reverseOrder()))
        .compare(lhs, rhs);
  }

  @VisibleForTesting
  static int compareReceivedFrom(ReceivedFrom lhs, ReceivedFrom rhs) {
    // Prefer lower neighbor IP, then break tie on ReceivedFrom subtype, then on
    // ReceivedFromInterface interface if applicable, else yield a tie.
    return Comparator.comparing(LegacyReceivedFromToIpConverter::convert, Comparator.reverseOrder())
        .thenComparing(RECEIVED_FROM_TYPE_PREFERENCE::visit)
        .thenComparing(BgpRib::compareReceivedFromInterface)
        .compare(lhs, rhs);
  }

  private static final ReceivedFromVisitor<Integer> RECEIVED_FROM_TYPE_PREFERENCE =
      new ReceivedFromVisitor<Integer>() {
        @Override
        public Integer visitReceivedFromIp(ReceivedFromIp receivedFromIp) {
          return 1;
        }

        @Override
        public Integer visitReceivedFromInterface(ReceivedFromInterface receivedFromInterface) {
          return 0;
        }

        @Override
        public Integer visitReceivedFromSelf() {
          return 2;
        }
      };

  private static int compareReceivedFromInterface(ReceivedFrom lhs, ReceivedFrom rhs) {
    // lhs and rhs should have same type if previously compared via type preference
    assert lhs.getClass().equals(rhs.getClass());
    if (!(lhs instanceof ReceivedFromInterface)) {
      return 0;
    }
    ReceivedFromInterface ilhs = (ReceivedFromInterface) lhs;
    ReceivedFromInterface irhs = (ReceivedFromInterface) rhs;
    return ilhs.getInterface().compareTo(irhs.getInterface());
  }

  /** Returns {@code true} iff this RIB is configured to be a BGP multipath RIB. */
  public boolean isMultipath() {
    return _maxPaths == null || _maxPaths > 1;
  }

  /** Establish total ordering: Prefer BGP aggregate routes */
  private static int getAggregatePreference(RoutingProtocol protocol) {
    if (protocol == RoutingProtocol.AGGREGATE) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Establish total ordering: BGP aggregate routes preferred over eBGP, which is preferred over
   * iBGP
   */
  private static int getTypeCost(RoutingProtocol protocol) {
    switch (protocol) {
      case AGGREGATE:
        return 2;
      case BGP: // eBGP
        return 1;
      case IBGP:
        return 0;
      default:
        throw new IllegalArgumentException(String.format("Invalid BGP protocol: %s", protocol));
    }
  }

  /**
   * Attempt to calculate the cost to reach given routes next hop IP.
   *
   * @param route bgp route
   * @return if next hop IP matches a route we have, returns the metric for that route; otherwise
   *     {@link Long#MAX_VALUE}
   */
  private long getIgpCostToNextHopIp(R route) {
    if (_mainRib == null) {
      return Long.MAX_VALUE;
    }
    return getIgpCostToNextHopIpHelper(route, 0);
  }

  private long getIgpCostToNextHopIpHelper(AbstractRoute route, int depth) {
    if (depth > MAX_RESOLUTION_DEPTH) {
      return Long.MAX_VALUE;
    }
    assert _mainRib != null;
    return route
        .getNextHop()
        .accept(
            new NextHopVisitor<Long>() {
              @Override
              public Long visitNextHopIp(NextHopIp nextHopIp) {
                // TODO: implement resolution restriction
                Set<AnnotatedRoute<AbstractRoute>> s =
                    _mainRib.longestPrefixMatch(nextHopIp.getIp(), alwaysTrue());
                return s.isEmpty()
                    ? Long.MAX_VALUE
                    : getIgpCostToNextHopIpHelper(
                        s.iterator().next().getAbstractRoute(), depth + 1);
              }

              @Override
              public Long visitNextHopInterface(NextHopInterface nextHopInterface) {
                return route.getMetric();
              }

              @Override
              public Long visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
                return Long.MAX_VALUE;
              }

              @Override
              public Long visitNextHopVrf(NextHopVrf nextHopVrf) {
                // TODO: something better?
                return Long.MAX_VALUE;
              }

              @Override
              public Long visitNextHopVtep(NextHopVtep nextHopVtep) {
                // TODO: something better?
                return Long.MAX_VALUE;
              }
            });
  }

  /**
   * return Cluster List Length for a given route, if appropriate. For non-FRR RIBs, we simply
   * return 0's here to guarantee equivalence.
   *
   * @param route bgp route
   * @return integer representing cluster list length. 0 by default.
   */
  private int getClusterListLength(R route) {
    if (!_clusterListAsIgpCost) {
      return 0;
    }
    return route.getClusterList().size();
  }

  @VisibleForTesting
  @Nonnull
  Map<R, Long> getArrivalTimeForTesting() {
    return Collections.unmodifiableMap(_logicalArrivalTime);
  }
}
