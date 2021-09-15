package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * A generic BGP RIB containing the common properties among the RIBs for different types of BGP
 * routes
 */
@ParametersAreNonnullByDefault
public abstract class BgpRib<R extends BgpRoute<?, ?>> extends AbstractRib<R> {

  private static final int MAX_RESOLUTION_DEPTH = 10;

  /** Main RIB to use for IGP cost estimation */
  @Nullable protected final GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> _mainRib;
  /** Tie breaker to use if all route attributes appear to be equal */
  @Nonnull protected final BgpTieBreaker _tieBreaker;
  /** Maximum number of paths to install. Unconstrained (infinite) if {@code null} */
  @Nullable protected final Integer _maxPaths;
  /**
   * For multipath: how strict should the comparison of AS Path be for paths to be considered equal
   */
  @Nullable protected final MultipathEquivalentAsPathMatchMode _multipathEquivalentAsPathMatchMode;
  // Best BGP paths. Invariant: must be re-evaluated (per prefix) each time a route is added or
  // evicted
  @Nonnull protected final Map<Prefix, R> _bestPaths;
  /**
   * This logical clock helps us keep track when routes were merged into the RIB to determine their
   * age. It's incremented each time a route is merged into the RIB.
   */
  protected long _logicalClock;
  /** Map to keep track when routes were merged in. */
  protected Map<R, Long> _logicalArrivalTime;

  /** For FRR, Cluster List Length is used as an IGP metric. */
  protected boolean _clusterListAsIgpCost;

  protected BgpRib(
      @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean withBackups,
      boolean clusterListAsIgpCost) {
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
      return Comparator.comparing(Function.identity(), this::bestPathComparator).compare(lhs, rhs);
    }
  }

  @Nonnull
  @Override
  public RibDelta<R> mergeRouteGetDelta(R route) {
    RibDelta<R> delta = super.mergeRouteGetDelta(route);
    if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER) {
      _logicalArrivalTime.put(route, _logicalClock);
      _logicalClock++;
    }
    if (!delta.isEmpty()) {
      delta.getPrefixes().forEach(this::selectBestPath);
    }
    return delta;
  }

  @Nonnull
  @Override
  public RibDelta<R> removeRouteGetDelta(R route, Reason reason) {
    RibDelta<R> delta = super.removeRouteGetDelta(route, reason);
    if (!delta.isEmpty()) {
      delta.getPrefixes().forEach(this::selectBestPath);
      delta
          .getActions()
          .forEach(
              a -> {
                if (_tieBreaker == BgpTieBreaker.ARRIVAL_ORDER && a.isWithdrawn()) {
                  _logicalArrivalTime.remove(a.getRoute());
                }
              });
    }
    return delta;
  }

  @Override
  @Nonnull
  public final Set<R> getTypedRoutes() {
    if (isMultipath()) {
      return super.getTypedRoutes();
    } else {
      return getBestPathRoutes();
    }
  }

  /** Whether a route is currently the best path for its prefix */
  public final boolean isBestPath(R route) {
    return route.equals(_bestPaths.get(route.getNetwork()));
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
    switch (_multipathEquivalentAsPathMatchMode) {
      case EXACT_PATH:
        return lhs.equals(rhs) ? 0 : -1;
      case FIRST_AS:
        AsSet lhsFirstAsSet = lhs.getAsSets().isEmpty() ? AsSet.empty() : lhs.getAsSets().get(0);
        AsSet rhsFirstAsSet = rhs.getAsSets().isEmpty() ? AsSet.empty() : rhs.getAsSets().get(0);
        return lhsFirstAsSet.equals(rhsFirstAsSet) ? 0 : -1;
      case PATH_LENGTH:
        assert lhs.length() == rhs.length();
        return 0;
      default:
        throw new IllegalStateException(
            String.format(
                "Unsupported AS PATH comparison mode: %s", _multipathEquivalentAsPathMatchMode));
    }
  }

  private void selectBestPath(Prefix prefix) {
    Optional<R> s = extractRoutes(prefix).stream().max(this::bestPathComparator);
    if (!s.isPresent()) {
      // Remove best path and return
      _bestPaths.remove(prefix);
      return;
    }
    _bestPaths.put(prefix, s.get());
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
        // Prefer lower neighbor IP
        .thenComparing(R::getReceivedFromIp, Comparator.nullsFirst(Comparator.reverseOrder()))
        .compare(lhs, rhs);
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
}
