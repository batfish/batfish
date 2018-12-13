package org.batfish.dataplane.protocols;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.WellKnownCommunity;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.exceptions.BgpRoutePropagationException;

public class BgpProtocolHelper {

  /**
   * Perform BGP export transformations on a given route when sending an advertisement from {@code
   * fromNeighbor} to {@code toNeighbor}
   */
  @Nullable
  public static BgpRoute.Builder transformBgpRouteOnExport(
      BgpPeerConfig fromNeighbor,
      BgpPeerConfig toNeighbor,
      BgpSessionProperties sessionProperties,
      Vrf fromVrf,
      Vrf toVrf,
      AbstractRoute route)
      throws BgpRoutePropagationException {

    BgpRoute.Builder transformedOutgoingRouteBuilder = new BgpRoute.Builder();

    // Set the tag
    transformedOutgoingRouteBuilder.setTag(route.getTag());

    transformedOutgoingRouteBuilder.setReceivedFromIp(fromNeighbor.getLocalIp());
    RoutingProtocol remoteRouteProtocol = route.getProtocol();

    boolean remoteRouteIsBgp =
        remoteRouteProtocol == RoutingProtocol.IBGP || remoteRouteProtocol == RoutingProtocol.BGP;
    RoutingProtocol targetProtocol =
        sessionProperties.isEbgp() ? RoutingProtocol.BGP : RoutingProtocol.IBGP;

    // Set originatorIP
    Ip originatorIp;
    if (!sessionProperties.isEbgp() && remoteRouteProtocol.equals(RoutingProtocol.IBGP)) {
      BgpRoute bgpRemoteRoute = (BgpRoute) route;
      originatorIp = bgpRemoteRoute.getOriginatorIp();
    } else {
      originatorIp = fromVrf.getBgpProcess().getRouterId();
    }
    transformedOutgoingRouteBuilder.setOriginatorIp(originatorIp);

    // note whether new route is received from route reflector client
    transformedOutgoingRouteBuilder.setReceivedFromRouteReflectorClient(
        !sessionProperties.isEbgp() && toNeighbor.getRouteReflectorClient());

    // clusterList, receivedFromRouteReflectorClient, (originType
    // for bgp remote route)
    if (remoteRouteIsBgp) {
      BgpRoute bgpRemoteRoute = (BgpRoute) route;

      if (bgpRemoteRoute.getCommunities().contains(WellKnownCommunity.NO_ADVERTISE)) {
        // do not export
        return null;
      }

      transformedOutgoingRouteBuilder.setOriginType(bgpRemoteRoute.getOriginType());
      if (sessionProperties.isEbgp()
          && bgpRemoteRoute.getAsPath().containsAs(toNeighbor.getLocalAs())
          && !fromNeighbor.getAllowRemoteAsOut()) {
        // skip routes containing peer's AS unless
        // disable-peer-as-check (getAllowRemoteAsOut) is set
        return null;
      }
      /*
       * route reflection: reflect everything received from
       * clients to clients and non-clients. reflect everything
       * received from non-clients to clients. Do not reflect to
       * originator
       */

      Ip remoteOriginatorIp = bgpRemoteRoute.getOriginatorIp();
      /*
       *  iBGP speaker should not send out routes to iBGP neighbor whose router-id is
       *  same as originator id of advertisement
       */
      if (!sessionProperties.isEbgp()
          && toVrf.getBgpProcess().getRouterId().equals(remoteOriginatorIp)) {
        return null;
      }
      if (remoteRouteProtocol.equals(RoutingProtocol.IBGP) && !sessionProperties.isEbgp()) {
        /*
         *  The remote route is iBGP. The session is iBGP. We consider whether to reflect, and
         *  modify the outgoing route as appropriate.
         */
        boolean remoteRouteReceivedFromRouteReflectorClient =
            bgpRemoteRoute.getReceivedFromRouteReflectorClient();
        boolean sendingToRouteReflectorClient = fromNeighbor.getRouteReflectorClient();
        Ip remoteReceivedFromIp = bgpRemoteRoute.getReceivedFromIp();
        boolean remoteRouteOriginatedByRemoteNeighbor = remoteReceivedFromIp.equals(Ip.ZERO);
        if (!remoteRouteReceivedFromRouteReflectorClient
            && !sendingToRouteReflectorClient
            && !remoteRouteOriginatedByRemoteNeighbor) {
          /*
           * Neither reflecting nor originating this iBGP route, so don't send
           */
          return null;
        }
        transformedOutgoingRouteBuilder.addClusterList(bgpRemoteRoute.getClusterList());
        if (!remoteRouteOriginatedByRemoteNeighbor) {
          // we are reflecting, so we need to get the clusterid associated with the
          // remoteRoute
          BgpPeerConfig remoteReceivedFromSession =
              fromVrf
                  .getBgpProcess()
                  .getActiveNeighbors()
                  .get(new Prefix(remoteReceivedFromIp, Prefix.MAX_PREFIX_LENGTH));
          long newClusterId = remoteReceivedFromSession.getClusterId();
          transformedOutgoingRouteBuilder.addToClusterList(newClusterId);
        }
        Set<Long> localClusterIds = toVrf.getBgpProcess().getClusterIds();
        Set<Long> outgoingClusterList = transformedOutgoingRouteBuilder.getClusterList();
        if (localClusterIds.stream().anyMatch(outgoingClusterList::contains)) {
          /*
           *  receiver will reject new route if it contains any of its local cluster ids
           */
          return null;
        }
      }
    }

    // Outgoing asPath
    // Outgoing communities
    if (remoteRouteIsBgp) {
      BgpRoute bgpRemoteRoute = (BgpRoute) route;
      transformedOutgoingRouteBuilder.setAsPath(bgpRemoteRoute.getAsPath());
      if (fromNeighbor.getSendCommunity()) {
        transformedOutgoingRouteBuilder.addCommunities(bgpRemoteRoute.getCommunities());
      }
    }
    if (sessionProperties.isEbgp()) {
      transformedOutgoingRouteBuilder.setAsPath(
          AsPath.of(
              ImmutableList.<AsSet>builder()
                  .add(AsSet.of(fromNeighbor.getLocalAs()))
                  .addAll(transformedOutgoingRouteBuilder.getAsPath().getAsSets())
                  .build()));
    }

    // Outgoing protocol
    transformedOutgoingRouteBuilder.setProtocol(targetProtocol);
    transformedOutgoingRouteBuilder.setNetwork(route.getNetwork());

    // Outgoing metric
    if (remoteRouteIsBgp) {
      transformedOutgoingRouteBuilder.setMetric(route.getMetric());
    }

    // Outgoing nextHopIp & localPreference
    Ip nextHopIp;
    long localPreference;
    if (sessionProperties.isEbgp() || !remoteRouteIsBgp) {
      nextHopIp = fromNeighbor.getLocalIp();
      localPreference = BgpRoute.DEFAULT_LOCAL_PREFERENCE;
    } else {
      nextHopIp = route.getNextHopIp();
      BgpRoute remoteIbgpRoute = (BgpRoute) route;
      localPreference = remoteIbgpRoute.getLocalPreference();
    }
    if (Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)) {
      // should only happen for ibgp or dynamic bgp
      if (fromNeighbor instanceof BgpPassivePeerConfig) {
        nextHopIp = ((BgpActivePeerConfig) toNeighbor).getPeerAddress();
      } else {
        String nextHopInterface = route.getNextHopInterface();
        InterfaceAddress nextHopAddress =
            fromVrf.getInterfaces().get(nextHopInterface).getAddress();
        if (nextHopAddress == null) {
          throw new BgpRoutePropagationException("Route's nextHopInterface has no address");
        }
        nextHopIp = nextHopAddress.getIp();
      }
    }
    transformedOutgoingRouteBuilder.setNextHopIp(nextHopIp);
    transformedOutgoingRouteBuilder.setLocalPreference(localPreference);

    // Outgoing srcProtocol
    transformedOutgoingRouteBuilder.setSrcProtocol(route.getProtocol());
    return transformedOutgoingRouteBuilder;
  }

  /** Perform BGP import transformations on a given route after receiving an advertisement */
  @Nullable
  public static BgpRoute.Builder transformBgpRouteOnImport(
      BgpPeerConfig toNeighbor,
      BgpSessionProperties sessionProperties,
      BgpRoute route,
      ConfigurationFormat configFormat) {

    if (route.getAsPath().containsAs(requireNonNull(toNeighbor.getLocalAs()))
        && !toNeighbor.getAllowLocalAsIn()) {
      // skip routes containing peer's AS unless
      // disable-peer-as-check (getAllowRemoteAsOut) is set
      return null;
    }
    RoutingProtocol targetProtocol =
        sessionProperties.isEbgp() ? RoutingProtocol.BGP : RoutingProtocol.IBGP;

    BgpRoute.Builder transformedIncomingRouteBuilder = new BgpRoute.Builder();
    transformedIncomingRouteBuilder.setOriginatorIp(route.getOriginatorIp());
    transformedIncomingRouteBuilder.setReceivedFromIp(route.getReceivedFromIp());
    transformedIncomingRouteBuilder.addClusterList(route.getClusterList());
    transformedIncomingRouteBuilder.setReceivedFromRouteReflectorClient(
        route.getReceivedFromRouteReflectorClient());
    transformedIncomingRouteBuilder.setAsPath(route.getAsPath());
    transformedIncomingRouteBuilder.addCommunities(route.getCommunities());
    transformedIncomingRouteBuilder.setProtocol(targetProtocol);
    transformedIncomingRouteBuilder.setNetwork(route.getNetwork());
    transformedIncomingRouteBuilder.setNextHopIp(route.getNextHopIp());
    transformedIncomingRouteBuilder.setLocalPreference(route.getLocalPreference());
    transformedIncomingRouteBuilder.setAdmin(
        targetProtocol.getDefaultAdministrativeCost(configFormat));
    transformedIncomingRouteBuilder.setMetric(route.getMetric());
    transformedIncomingRouteBuilder.setOriginType(route.getOriginType());
    transformedIncomingRouteBuilder.setSrcProtocol(targetProtocol);

    return transformedIncomingRouteBuilder;
  }

  /**
   * Convert an aggregate/generated route to a BGP route.
   *
   * @param generatedRoute a {@link GeneratedRoute} to convert to a {@link BgpRoute}.
   * @param routerId Router ID to set as the originatorIp for the resulting BGP route.
   */
  public static BgpRoute convertGeneratedRouteToBgp(GeneratedRoute generatedRoute, Ip routerId) {
    BgpRoute.Builder b = new BgpRoute.Builder();
    b.setAdmin(generatedRoute.getAdministrativeCost());
    b.setAsPath(generatedRoute.getAsPath());
    b.setCommunities(generatedRoute.getCommunities());
    b.setMetric(generatedRoute.getMetric());
    b.setSrcProtocol(RoutingProtocol.AGGREGATE);
    b.setProtocol(RoutingProtocol.AGGREGATE);
    b.setNetwork(generatedRoute.getNetwork());
    b.setLocalPreference(BgpRoute.DEFAULT_LOCAL_PREFERENCE);
    /*
     * Note: Origin type and originator IP should get overwritten by export policy,
     * but are needed initially
     */
    b.setOriginatorIp(routerId);
    b.setOriginType(OriginType.INCOMPLETE);
    b.setReceivedFromIp(Ip.ZERO);
    return b.build();
  }
}
