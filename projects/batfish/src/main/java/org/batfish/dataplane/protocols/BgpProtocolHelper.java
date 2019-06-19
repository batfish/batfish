package org.batfish.dataplane.protocols;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.WellKnownCommunity;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;

@ParametersAreNonnullByDefault
public final class BgpProtocolHelper {

  /**
   * Perform BGP export transformations on a given route when sending an advertisement from {@code
   * fromNeighbor} to {@code toNeighbor} before export policy is applied.
   *
   * @param fromNeighbor {@link BgpPeerConfig} exporting {@code route}
   * @param toNeighbor {@link BgpPeerConfig} to which to export {@code route}
   * @param sessionProperties {@link BgpSessionProperties} representing the <em>incoming</em> edge:
   *     i.e. the edge from {@code toNeighbor} to {@code fromNeighbor}
   */
  @Nullable
  public static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      B transformBgpRoutePreExport(
          BgpPeerConfig fromNeighbor,
          BgpPeerConfig toNeighbor,
          BgpSessionProperties sessionProperties,
          BgpProcess fromBgpProcess,
          BgpProcess toBgpProcess,
          BgpRoute<B, R> route) {

    // Make a new builder
    B builder = route.toBuilder();

    // sessionProperties represents incoming edge, so fromNeighbor's IP is its headIp
    Ip fromNeighborIp = sessionProperties.getHeadIp();
    RoutingProtocol routeProtocol = route.getProtocol();

    builder.setReceivedFromIp(fromNeighborIp);
    builder.setProtocol(sessionProperties.isEbgp() ? RoutingProtocol.BGP : RoutingProtocol.IBGP);
    builder.setSrcProtocol(route.getProtocol());

    // Clear a bunch of non-transitive attributes
    builder.setWeight(0);
    builder.setDiscard(false);
    builder.setNonRouting(false);
    builder.setNonForwarding(false);
    builder.setAdmin(toBgpProcess.getAdminCost(builder.getProtocol()));
    builder.setTag(null);

    // Set originatorIP
    if (sessionProperties.isEbgp() || !routeProtocol.equals(RoutingProtocol.IBGP)) {
      // eBGP session and not iBGP route: override the originator
      builder.setOriginatorIp(fromBgpProcess.getRouterId());
    }

    // note whether new route is received from route reflector client
    builder.setReceivedFromRouteReflectorClient(
        !sessionProperties.isEbgp() && toNeighbor.getRouteReflectorClient());

    SortedSet<Community> communities = route.getCommunities();
    // Do not export route if it has NO_ADVERTISE community, or if its AS path contains the remote
    // peer's AS and local peer has not set getAllowRemoteOut
    if (communities.contains(StandardCommunity.of(WellKnownCommunity.NO_ADVERTISE))
        || (sessionProperties.isEbgp()
            && route.getAsPath().containsAs(toNeighbor.getLocalAs())
            && !fromNeighbor.getAllowRemoteAsOut())) {
      return null;
    }

    // Set transformed route's communities
    if (fromNeighbor.getSendCommunity()) {
      builder.addCommunities(communities);
    } else {
      builder.setCommunities(ImmutableSet.of());
    }

    /*
     *  iBGP speaker should not send out routes to iBGP neighbor whose router-id is
     *  same as originator id of advertisement
     */
    if (!sessionProperties.isEbgp() && toBgpProcess.getRouterId().equals(route.getOriginatorIp())) {
      return null;
    }

    builder.setClusterList(ImmutableSet.of());
    if (routeProtocol.equals(RoutingProtocol.IBGP) && !sessionProperties.isEbgp()) {
      /*
       * The remote route is iBGP. The session is iBGP. We consider whether to reflect, and
       * modify the outgoing route as appropriate.
       *
       * For route reflection: reflect everything received from
       * clients to clients and non-clients. reflect everything
       * received from non-clients to clients. Do not reflect to
       * originator
       */
      boolean remoteRouteReceivedFromRouteReflectorClient =
          route.getReceivedFromRouteReflectorClient();
      boolean sendingToRouteReflectorClient = fromNeighbor.getRouteReflectorClient();
      Ip remoteReceivedFromIp = route.getReceivedFromIp();
      boolean remoteRouteOriginatedByRemoteNeighbor = Ip.ZERO.equals(remoteReceivedFromIp);
      if (!remoteRouteReceivedFromRouteReflectorClient
          && !sendingToRouteReflectorClient
          && !remoteRouteOriginatedByRemoteNeighbor) {
        /*
         * Neither reflecting nor originating this iBGP route, so don't send
         */
        return null;
      }
      builder.addClusterList(route.getClusterList());
      if (!remoteRouteOriginatedByRemoteNeighbor) {
        // we are reflecting, so we need to get the clusterid associated with the
        // remoteRoute
        BgpPeerConfig remoteReceivedFromSession =
            fromBgpProcess
                .getActiveNeighbors()
                .get(Prefix.create(remoteReceivedFromIp, Prefix.MAX_PREFIX_LENGTH));
        assert remoteReceivedFromSession != null;
        Long newClusterId = remoteReceivedFromSession.getClusterId();
        if (newClusterId != null) {
          builder.addToClusterList(newClusterId);
        }
      }
      Set<Long> localClusterIds = toBgpProcess.getClusterIds();
      Set<Long> outgoingClusterList = builder.getClusterList();
      if (localClusterIds.stream().anyMatch(outgoingClusterList::contains)) {
        /*
         *  receiver will reject new route if it contains any of its local cluster ids
         */
        return null;
      }
    }

    // Outgoing metric (MED) is preserved only if advertising to IBGP peer. For eBGP, clear it.
    if (sessionProperties.isEbgp()) {
      builder.setMetric(0);
    }

    // Local preference: only transitive for iBGP
    builder.setLocalPreference(
        sessionProperties.isEbgp()
            ? BgpRoute.DEFAULT_LOCAL_PREFERENCE
            : route.getLocalPreference());

    // Outgoing nextHopIp
    if (sessionProperties.isEbgp()) {
      // If session is eBGP, always override next-hop
      builder.setNextHopIp(fromNeighborIp);
    } else {
      // iBGP session: if route has next-hop ip, preserve it. If not, set our own.
      // Note: implementation of next-hop-self in the general case is delegated to routing policy
      builder.setNextHopIp(
          route.getNextHopIp().equals(Route.UNSET_ROUTE_NEXT_HOP_IP)
              ? fromNeighborIp
              : route.getNextHopIp());
    }

    return builder;
  }

  /**
   * Perform BGP import transformations on a given route after receiving an advertisement.
   *
   * <p>Return {@code null} if the route should not be imported.
   */
  @Nullable
  public static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      B transformBgpRouteOnImport(
          BgpRoute<B, R> route,
          Long localAs,
          boolean allowLocalAsIn,
          boolean isEbgp,
          BgpProcess toProcess,
          @Nullable String peerInterface) {
    // skip routes containing peer's AS unless explicitly allowed
    if (route.getAsPath().containsAs(localAs) && !allowLocalAsIn) {
      return null;
    }

    RoutingProtocol targetProtocol = isEbgp ? RoutingProtocol.BGP : RoutingProtocol.IBGP;
    B builder = route.toBuilder();

    if (peerInterface != null) {
      builder.setNextHopInterface(peerInterface);
    }
    builder.setAdmin(toProcess.getAdminCost(targetProtocol));
    builder.setProtocol(targetProtocol);
    builder.setSrcProtocol(targetProtocol);

    return builder;
  }

  /**
   * Convert an aggregate/generated route to a BGP route.
   *
   * @param generatedRoute a {@link GeneratedRoute} to convert to a {@link Bgpv4Route}.
   * @param routerId Router ID to set as the originatorIp for the resulting BGP route.
   * @param nonRouting Whether to mark the Bgpv4Route as non-routing
   */
  @Nonnull
  public static Bgpv4Route convertGeneratedRouteToBgp(
      GeneratedRoute generatedRoute, Ip routerId, boolean nonRouting) {
    return Bgpv4Route.builder()
        .setAdmin(generatedRoute.getAdministrativeCost())
        .setAsPath(generatedRoute.getAsPath())
        .setCommunities(generatedRoute.getCommunities())
        .setMetric(generatedRoute.getMetric())
        .setSrcProtocol(RoutingProtocol.AGGREGATE)
        .setProtocol(RoutingProtocol.AGGREGATE)
        .setNetwork(generatedRoute.getNetwork())
        .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
        /*
         * Note: Origin type and originator IP should get overwritten by export policy,
         * but are needed initially
         */
        .setOriginatorIp(routerId)
        .setOriginType(OriginType.INCOMPLETE)
        .setReceivedFromIp(Ip.ZERO)
        .setNonRouting(nonRouting)
        .build();
  }

  /**
   * Convert a route that is neither a {@link BgpRoute} nor a {@link GeneratedRoute} to a {@link
   * Bgpv4Route.Builder}.
   *
   * <p>Intended for converting main RIB routes into their BGP equivalents before passing {@code
   * routeDecorator} to the export policy
   *
   * <p>The builder returned will will have default local preference, incomplete origin type, and
   * most other fields unset.
   */
  @Nonnull
  public static Bgpv4Route.Builder convertNonBgpRouteToBgpRoute(
      AbstractRouteDecorator routeDecorator,
      Ip routerId,
      Ip nextHopIp,
      int adminDistance,
      RoutingProtocol protocol) {
    assert protocol == RoutingProtocol.BGP || protocol == RoutingProtocol.IBGP;
    assert !(routeDecorator.getAbstractRoute() instanceof BgpRoute);
    AbstractRoute route = routeDecorator.getAbstractRoute();
    return Bgpv4Route.builder()
        .setNetwork(route.getNetwork())
        .setAdmin(adminDistance)
        .setOriginatorIp(routerId)
        .setProtocol(protocol)
        .setSrcProtocol(route.getProtocol())
        .setOriginType(OriginType.INCOMPLETE)
        // TODO: support customization of route preference
        .setLocalPreference(BgpRoute.DEFAULT_LOCAL_PREFERENCE)
        .setReceivedFromIp(protocol == RoutingProtocol.BGP ? nextHopIp : Ip.ZERO)
        .setNextHopIp(nextHopIp);
    // Let everything else default to unset/empty/etc.
  }

  /**
   * Perform BGP export transformations on a given route when sending an advertisement from {@code
   * fromNeighbor} to {@code toNeighbor} after export policy as applied and route is accepted, but
   * before route is sent onto the wire.
   */
  public static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      void transformBgpRoutePostExport(
          B routeBuilder, BgpPeerConfig fromNeighbor, BgpSessionProperties sessionProperties) {
    if (sessionProperties.isEbgp()) {
      // if eBGP, prepend as-path sender's as-path number
      routeBuilder.setAsPath(
          AsPath.of(
              ImmutableList.<AsSet>builder()
                  .add(AsSet.of(fromNeighbor.getLocalAs()))
                  .addAll(routeBuilder.getAsPath().getAsSets())
                  .build()));
    }
  }

  private BgpProtocolHelper() {}
}
