package org.batfish.dataplane.rib;

import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
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
      return _bgpRoutesByNhip.get(nhip);
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

  public Bgpv4Rib(
      @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean clusterListAsIgpCost) {
    super(
        mainRib,
        tieBreaker,
        maxPaths,
        multipathEquivalentAsPathMatchMode,
        true,
        clusterListAsIgpCost);
    _resolvabilityEnforcer = new ResolvabilityEnforcer();
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
    }
    return super.mergeRouteGetDelta(route);
  }

  @Nonnull
  @Override
  public RibDelta<Bgpv4Route> removeRouteGetDelta(Bgpv4Route route) {
    // Remove route from resolvability enforcer so it can't get reactivated.
    // No effect if the main RIB is null or if the route doesn't need resolving.
    _resolvabilityEnforcer.removeBgpRoute(route);
    return super.removeRouteGetDelta(route);
  }

  public RibDelta<Bgpv4Route> updateActiveRoutes(
      RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta) {
    // Should only be null in tests, and those tests shouldn't be using this function
    assert _mainRib != null;

    // Update resolvability enforcer's record of main RIB prefixes
    _resolvabilityEnforcer.updateMainRibPrefixes(mainRibDelta);

    // (De)activate BGP routes based on updated main RIB
    RibDelta.Builder<Bgpv4Route> delta = RibDelta.builder();
    _resolvabilityEnforcer
        .getAffectedNextHopIps(mainRibDelta.getPrefixes())
        .flatMap(
            nhip -> {
              boolean resolvable = isResolvable(nhip);
              return _resolvabilityEnforcer.getRoutesWithNhip(nhip).stream()
                  .map(
                      r -> resolvable ? super.mergeRouteGetDelta(r) : super.removeRouteGetDelta(r));
            })
        .forEach(delta::from);
    return delta.build();
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
    if (route.getProtocol() == RoutingProtocol.AGGREGATE) {
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
