package org.batfish.dataplane.rib;

import java.util.Optional;
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
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;

/** BGPv4-specific RIB implementation */
@ParametersAreNonnullByDefault
public final class Bgpv4Rib extends BgpRib<Bgpv4Route> {

  public Bgpv4Rib(
      @Nullable GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean withBackups,
      boolean clusterListAsIgpCost) {
    super(
        mainRib,
        tieBreaker,
        maxPaths,
        multipathEquivalentAsPathMatchMode,
        withBackups,
        clusterListAsIgpCost);
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
    if (shouldCheckNextHopReachability(route)
        .map(nextHopIp -> _mainRib != null && _mainRib.longestPrefixMatch(nextHopIp).isEmpty())
        .orElse(false)) {
      /*
      TODO: when backups are enabled again, we should probably put this in as a backup
         and then return empty delta
      */
      return RibDelta.empty();
    }
    return super.mergeRouteGetDelta(route);
  }

  /**
   * Returns the next hop IP for which to check reachability (i.e., look for route in main RIB), if
   * applicable. If reachability should not be checked returns {@link Optional#empty}
   */
  private static Optional<Ip> shouldCheckNextHopReachability(Bgpv4Route route) {
    if (route.getProtocol() == RoutingProtocol.AGGREGATE) {
      return Optional.empty();
    }
    return NEXT_HOP_REACHABILITY_VISITOR.visit(route.getNextHop());
  }

  private static final NextHopVisitor<Optional<Ip>> NEXT_HOP_REACHABILITY_VISITOR =
      new NextHopVisitor<Optional<Ip>>() {

        @Override
        public Optional<Ip> visitNextHopIp(NextHopIp nextHopIp) {
          // Real IP, we should make sure we can reach it.
          return Optional.of(nextHopIp.getIp());
        }

        @Override
        public Optional<Ip> visitNextHopInterface(NextHopInterface nextHopInterface) {
          // Next hop-interface. Likely BGP unnumbered, nothing to check
          return Optional.empty();
        }

        @Override
        public Optional<Ip> visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          // Discard route, nothing to check
          return Optional.empty();
        }

        @Override
        public Optional<Ip> visitNextHopVrf(NextHopVrf nextHopVrf) {
          // The next hop is in a different VRF, so nothing to check in current main RIB
          return Optional.empty();
        }
      };
}
