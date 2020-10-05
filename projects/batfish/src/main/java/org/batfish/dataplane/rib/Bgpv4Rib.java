package org.batfish.dataplane.rib;

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
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;

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
    Ip nextHopIp = route.getNextHopIp();
    /*
      Do not merge routes for which next hop is not reachable.
      However, due to some complications with how we create routes, we must skip this check for:
      - routes with link-local address as next hop (i.e., next-hop interface is set to something)
      - routes with Ip.AUTO as next hop or protocol AGGREGATE (for locally-generated routes/aggregates)
    */
    if (Route.UNSET_NEXT_HOP_INTERFACE.equals(route.getNextHopInterface())
        && !nextHopIp.equals(Ip.AUTO)
        && route.getProtocol() != RoutingProtocol.AGGREGATE
        && _mainRib != null
        && _mainRib.longestPrefixMatch(nextHopIp).isEmpty()) {
      /*
      TODO: when backups are enabled again, we should probably put this in as a backup
         and then return empty delta
      */
      return RibDelta.empty();
    }
    return super.mergeRouteGetDelta(route);
  }
}
