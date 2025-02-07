package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;
import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_WEIGHT;
import static org.batfish.datamodel.OriginMechanism.GENERATED;
import static org.batfish.datamodel.OriginMechanism.LEARNED;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.ReceivedFromInterface;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.BgpTopologyUtils.ConfedSessionType;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

@ParametersAreNonnullByDefault
public final class BgpProtocolHelper {

  /**
   * Perform BGP export transformations on a given route when sending an advertisement from {@code
   * localNeighbor} to {@code remoteNeighbor} before export policy is applied.
   *
   * @param localNeighbor {@link BgpPeerConfig} exporting {@code route}
   * @param remoteNeighbor {@link BgpPeerConfig} to which to export {@code route}
   * @param localSessionProperties {@link BgpSessionProperties} representing the <em>outgoing</em>
   *     edge: i.e. the edge from {@code localNeighbor} to {@code remoteNeighbor}
   * @param afType {@link AddressFamily.Type} the address family for which to look up the settings
   */
  public static @Nullable <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      B transformBgpRoutePreExport(
          BgpPeerConfig localNeighbor,
          BgpPeerConfig remoteNeighbor,
          BgpSessionProperties localSessionProperties,
          BgpProcess localBgpProcess,
          BgpProcess remoteBgpProcess,
          BgpRoute<B, R> route,
          Type afType) {

    // Make a new builder
    B builder = route.toBuilder();
    // this will be set later during export policy transformation or after it is exported
    builder.clearNextHop();

    RoutingProtocol routeProtocol = route.getProtocol();
    RoutingProtocol outgoingProtocol =
        localSessionProperties.isEbgp() ? RoutingProtocol.BGP : RoutingProtocol.IBGP;
    builder.setProtocol(outgoingProtocol);
    builder.setSrcProtocol(routeProtocol);

    // Clear a bunch of non-transitive attributes
    builder.setWeight(0);
    if (!(route instanceof EvpnRoute<?, ?>)) {
      // These attributes are constants for EVPN routes and cannot be set
      builder.setNonRouting(false);
      builder.setNonForwarding(false);
      builder.setAdmin(remoteBgpProcess.getAdminCost(outgoingProtocol));
    }
    builder.setTag(null);

    // Set originatorIP
    if (localSessionProperties.isEbgp() || !routeProtocol.equals(RoutingProtocol.IBGP)) {
      // eBGP session or not iBGP route: override the originator
      builder.setOriginatorIp(localBgpProcess.getRouterId());
    }

    AddressFamily af = localNeighbor.getAddressFamily(afType);
    assert af != null;

    // Do not export route if it has NO_ADVERTISE community.
    if (route.getCommunities().getCommunities().contains(StandardCommunity.NO_ADVERTISE)) {
      return null;
    }

    // For eBGP, do not export if AS path contains the peer's AS in a disallowed position
    if (localSessionProperties.isEbgp()
        && !allowAsPathOut(
            route.getAsPath(),
            localSessionProperties.getRemoteAs(),
            af.getAddressFamilyCapabilities().getAllowRemoteAsOut())) {
      return null;
    }
    // Also do not export if route has NO_EXPORT community and this is a true ebgp session
    if (route.getCommunities().getCommunities().contains(StandardCommunity.NO_EXPORT)
        && localSessionProperties.isEbgp()
        && localSessionProperties.getConfedSessionType() != ConfedSessionType.WITHIN_CONFED) {
      return null;
    }

    /*
     *  iBGP speaker should not send out routes to iBGP neighbor whose router-id is
     *  same as originator id of advertisement
     */
    if (!localSessionProperties.isEbgp()
        && remoteBgpProcess.getRouterId().equals(route.getOriginatorIp())) {
      return null;
    }

    builder.setClusterList(ImmutableSet.of());
    boolean routeOriginatedLocally = route.getReceivedFrom().equals(ReceivedFromSelf.instance());
    if (routeProtocol.equals(RoutingProtocol.IBGP) && !localSessionProperties.isEbgp()) {
      /*
       * The remote route is iBGP. The session is iBGP. We consider whether to reflect, and
       * modify the outgoing route as appropriate.
       */
      if (!isReflectable(
              route, localBgpProcess.getClientToClientReflection(), localSessionProperties, af)
          && !routeOriginatedLocally) {
        /*
         * Neither reflecting nor originating this iBGP route, so don't send
         */
        return null;
      }
      builder.addClusterList(route.getClusterList());
      if (!routeOriginatedLocally) {
        // we are reflecting, so we need to get the clusterid associated with the
        // remoteRoute
        Long newClusterId = localNeighbor.getClusterId();
        if (newClusterId != null) {
          builder.addToClusterList(newClusterId);
        }
      }
      Set<Long> localClusterIds = remoteBgpProcess.getClusterIds();
      Set<Long> outgoingClusterList = builder.getClusterList();
      if (localClusterIds.stream().anyMatch(outgoingClusterList::contains)) {
        /*
         *  receiver will reject new route if it contains any of its local cluster ids
         */
        return null;
      }
    }

    // Outgoing metric (MED) is preserved only if advertising to IBGP peer, within a confederation,
    // or for locally originated routes
    if (!localSessionProperties.advertiseUnchangedMed() && !routeOriginatedLocally) {
      builder.setMetric(0);
    }

    // Local preference: only transitive for iBGP or within a confederation
    builder.setLocalPreference(
        localSessionProperties.advertiseUnchangedLocalPref()
            ? route.getLocalPreference()
            : DEFAULT_LOCAL_PREFERENCE);

    return builder;
  }

  /*
   * Ensure the remote route is iBGP and the session is iBGP. We consider whether to reflect.
   *
   * For route reflection: reflect everything received from
   * clients to clients and non-clients. reflect everything
   * received from non-clients to clients.
   */
  public static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>> boolean isReflectable(
      BgpRoute<B, R> route,
      boolean clientToClientReflection,
      BgpSessionProperties session,
      AddressFamily localAf) {
    switch (session.getSessionType()) {
      case IBGP:
      case IBGP_UNNUMBERED:
        break;
      default:
        return false;
    }

    if (!route.getProtocol().equals(RoutingProtocol.IBGP)) {
      return false;
    }

    if (route.getReceivedFromRouteReflectorClient()) {
      // Advertise routes learned from Route Reflector clients to other clients only if
      // client-to-client reflection is enabled. Non-RR clients get reflected routes
      // unconditionally.
      return clientToClientReflection || !localAf.getRouteReflectorClient();
    }

    // Advertise routes from RR non-clients to RR clients only.
    return localAf.getRouteReflectorClient();
  }

  /**
   * Return {@code true} if an outgoing eBGP advertisement with given {@code asPath} to {@code
   * peerAs} should be allowed under the given {@code mode}.
   */
  @VisibleForTesting
  static boolean allowAsPathOut(AsPath asPath, long peerAs, AllowRemoteAsOutMode mode) {
    List<AsSet> asSets = asPath.getAsSets();
    if (asPath.getAsSets().isEmpty()) {
      return true;
    }
    return switch (mode) {
      case ALWAYS -> true;
      case NEVER -> asSets.stream().noneMatch(asSet -> asSet.containsAs(peerAs));
      case EXCEPT_FIRST -> !asSets.get(0).containsAs(peerAs);
    };
  }

  /**
   * Perform BGP import transformations on a given route after receiving an advertisement.
   *
   * <p>Return {@code null} if the route should not be imported.
   */
  public static @Nullable <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      B transformBgpRouteOnImport(
          BgpRoute<B, R> route,
          long localAs,
          boolean allowLocalAsIn,
          boolean isEbgp,
          BgpProcess toProcess,
          Ip peerIp,
          @Nullable String peerInterface) {
    // skip routes containing peer's AS unless explicitly allowed
    if (!allowLocalAsIn && route.getAsPath().containsAs(localAs)) {
      return null;
    }

    RoutingProtocol targetProtocol = isEbgp ? RoutingProtocol.BGP : RoutingProtocol.IBGP;

    B importBuilder = route.toBuilder();
    if (!(route instanceof EvpnRoute<?, ?>)) {
      // Only set admin for non-EVPN routes; EVPN routes have a constant admin distance.
      // Only set next hop for non-EVPN routes; EVPN routes come in with VTEP next hops.
      importBuilder
          .setAdmin(toProcess.getAdminCost(targetProtocol))
          .setNextHop(NextHop.legacyConverter(peerInterface, route.getNextHopIp()));
    }
    ReceivedFrom receivedFrom =
        peerInterface != null
            ? ReceivedFromInterface.of(peerInterface, peerIp)
            : ReceivedFromIp.of(peerIp);
    return importBuilder
        .setProtocol(targetProtocol)
        .setReceivedFrom(receivedFrom)
        .setSrcProtocol(targetProtocol)
        .setOriginMechanism(LEARNED);
  }

  /**
   * Convert an aggregate/generated route to a BGP route.
   *
   * @param generatedRoute a {@link GeneratedRoute} to convert to a {@link Bgpv4Route}.
   * @param attributePolicy a {@link RoutingPolicy} to use to set BGP route attributes after
   *     conversion
   * @param routerId Router ID to set as the originatorIp for the resulting BGP route.
   * @param nonRouting Whether to mark the Bgpv4Route as non-routing
   */
  public static @Nonnull Bgpv4Route convertGeneratedRouteToBgp(
      GeneratedRoute generatedRoute,
      @Nullable RoutingPolicy attributePolicy,
      Ip routerId,
      NextHop nextHop,
      boolean nonRouting) {
    Builder builder = convertGeneratedRouteToBgp(generatedRoute, routerId, nextHop, nonRouting);
    if (attributePolicy == null) {
      return builder.build();
    }
    boolean accepted =
        attributePolicy.process(builder.build(), builder.clearNextHop(), Direction.OUT);
    assert accepted;
    return builder.setNextHop(nextHop).build();
  }

  /**
   * Convert an aggregate/generated route to a BGP route builder.
   *
   * @param generatedRoute a {@link GeneratedRoute} to convert to a {@link Bgpv4Route}.
   * @param routerId Router ID to set as the originatorIp for the resulting BGP route.
   * @param nonRouting Whether to mark the Bgpv4Route as non-routing
   */
  @VisibleForTesting
  static @Nonnull Builder convertGeneratedRouteToBgp(
      GeneratedRoute generatedRoute, Ip routerId, NextHop nextHop, boolean nonRouting) {
    return Bgpv4Route.builder()
        .setAdmin(generatedRoute.getAdministrativeCost())
        .setAsPath(generatedRoute.getAsPath())
        .setCommunities(generatedRoute.getCommunities())
        .setMetric(generatedRoute.getMetric())
        .setSrcProtocol(RoutingProtocol.AGGREGATE)
        .setProtocol(RoutingProtocol.AGGREGATE)
        .setNextHop(nextHop)
        .setNetwork(generatedRoute.getNetwork())
        .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
        /*
         * Note: Origin type and originator IP should get overwritten by export policy,
         * but are needed initially
         */
        .setOriginatorIp(routerId)
        .setOriginMechanism(GENERATED)
        .setOriginType(generatedRoute.getOriginType())
        .setReceivedFrom(/* Originated locally. */ ReceivedFromSelf.instance())
        .setNonRouting(nonRouting);
  }

  /** Create a BGP route from an activated aggregate. */
  public static @Nonnull Bgpv4Route toBgpv4Route(
      BgpAggregate aggregate, @Nullable RoutingPolicy attributePolicy, int admin, Ip routerId) {
    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setAdmin(admin)
            // TODO: support merging as-path from contributors via generationPolicy
            .setAsPath(AsPath.empty())
            // TODO: support merging communities from contributors via generationPolicy
            .setCommunities(CommunitySet.empty())
            .setMetric(0L)
            .setSrcProtocol(RoutingProtocol.AGGREGATE)
            .setProtocol(RoutingProtocol.AGGREGATE)
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(aggregate.getNetwork())
            .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
            .setOriginatorIp(routerId)
            .setOriginMechanism(GENERATED)
            // TODO: confirm default is IGP for all devices initializing aggregates from BGP RIB
            .setOriginType(OriginType.IGP)
            .setReceivedFrom(/* Originated locally. */ ReceivedFromSelf.instance())
            .setWeight(DEFAULT_LOCAL_WEIGHT);
    if (attributePolicy == null) {
      return builder.build();
    }
    boolean accepted = attributePolicy.process(builder.build(), builder, Direction.OUT);
    assert accepted;
    return builder.build();
  }

  /**
   * Convert a route that is neither a {@link BgpRoute} nor a {@link GeneratedRoute} to a {@link
   * Bgpv4Route.Builder}.
   *
   * <p>Intended for converting main RIB routes into their BGP equivalents before passing {@code
   * routeDecorator} to the export policy
   *
   * <p>The builder returned will have default local preference, redistribute origin mechanism,
   * incomplete origin type, and most other fields unset.
   */
  public static @Nonnull Bgpv4Route.Builder convertNonBgpRouteToBgpRoute(
      AbstractRouteDecorator routeDecorator,
      Ip routerId,
      Ip nextHopIp,
      int adminDistance,
      RoutingProtocol protocol,
      OriginMechanism originMechanism) {
    assert protocol == RoutingProtocol.BGP || protocol == RoutingProtocol.IBGP;
    assert !(routeDecorator.getAbstractRoute() instanceof BgpRoute);
    AbstractRoute route = routeDecorator.getAbstractRoute();
    return Bgpv4Route.builder()
        .setNetwork(route.getNetwork())
        .setAdmin(adminDistance)
        .setOriginatorIp(routerId)
        .setProtocol(protocol)
        .setSrcProtocol(route.getProtocol())
        .setOriginMechanism(originMechanism)
        .setOriginType(OriginType.INCOMPLETE)
        // TODO: support customization of route preference
        .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
        .setReceivedFrom(/* Originated locally. */ ReceivedFromSelf.instance())
        .setNextHopIp(nextHopIp)
        .setMetric(route.getMetric())
        .setTag(routeDecorator.getAbstractRoute().getTag());
    // Let everything else default to unset/empty/etc.
  }

  /**
   * Perform BGP export transformations on a given route <em>after</em> export policy has been
   * applied to the route, route was accepted, but before route is sent "onto the wire".
   *
   * <p>Sets next hop - if not already set by export policy - for non-EVPN-type-5 routes only. EVPN
   * type 5 routes' next hops should be set by {@link #setEvpnType5NhPostExport}.
   *
   * <p>Sets path ID if our session supports sending additional paths, otherwise clears it.
   *
   * @param originalRoute Original route being exported. Will be used as a key in routesToPathIds if
   *     a path ID is warranted
   * @param routeBuilder Builder for the output (exported) route
   * @param ourSessionProperties properties for the sender's session
   * @param af sender's address family configuration
   * @param originalRouteNhip BGP next hop IP of the original route, or {@link
   *     Route#UNSET_ROUTE_NEXT_HOP_IP} if original route is not BGP
   * @param pathIdGenerators Used for generating a path ID for the outgoing advertisement if needed
   * @param routesToPathIds Routes we have already exported, mapped to the path IDs with which we
   *     exported them. If the outgoing route needs a path ID, we will look up {@code originalRoute}
   *     in this map to find the correct path ID, and add a new entry if it isn't already there.
   */
  public static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      void transformBgpRoutePostExport(
          AbstractRouteDecorator originalRoute,
          B routeBuilder,
          BgpSessionProperties ourSessionProperties,
          AddressFamily af,
          Ip originalRouteNhip,
          Map<Prefix, Integer> pathIdGenerators,
          Map<AbstractRouteDecorator, Integer> routesToPathIds) {
    // Determine path ID to export, if any.
    Integer pathId = null;
    if (ourSessionProperties.getAdditionalPaths()) {
      pathId =
          routesToPathIds.computeIfAbsent(
              originalRoute,
              k ->
                  // pathIdGenerators is a concurrent map; must use compute rather than get/put
                  pathIdGenerators.compute(
                      originalRoute.getNetwork(), (p, lastId) -> lastId == null ? 1 : lastId + 1));
    }
    transformBgpRoutePostExport(
        routeBuilder,
        ourSessionProperties.isEbgp(),
        af.getAddressFamilyCapabilities().getSendCommunity(),
        af.getAddressFamilyCapabilities().getSendExtendedCommunity(),
        ourSessionProperties.getConfedSessionType(),
        ourSessionProperties.getLocalAs(),
        ourSessionProperties.getLocalIp(),
        originalRouteNhip,
        pathId,
        ourSessionProperties.getReplaceNonLocalAsesOnExport());
  }

  /**
   * Perform BGP export transformations on a given route <em>after</em> export policy has been
   * applied to the route, route was accepted, but before route is sent "onto the wire".
   *
   * <p>Sets next hop - if not already set by export policy - for non-EVPN-type-5 routes only. EVPN
   * type 5 routes' next hops should be set by {@link #setEvpnType5NhPostExport}.
   *
   * @param routeBuilder Builder for the output (exported) route
   * @param isEbgp true for ebgp sessions
   * @param sendStandardCommunities whether to send standard communities to the neighbor
   * @param sendExtendedCommunities whether to send extended communities to the neighbor
   * @param confedSessionType type of confederation session, if any
   * @param localAs local AS of the neighbor which is exporting the route, in that neighbor's config
   * @param localIp IP of the neighbor which is exporting the route
   * @param originalRouteNhip Next hop IP of the original route
   * @param replaceAllAsesWithLocalAs whether to hide the AS path details by replacing all AsSet
   *     elements with the localAs
   */
  @VisibleForTesting
  static <R extends BgpRoute<B, R>, B extends BgpRoute.Builder<B, R>>
      void transformBgpRoutePostExport(
          B routeBuilder,
          boolean isEbgp,
          boolean sendStandardCommunities,
          boolean sendExtendedCommunities,
          ConfedSessionType confedSessionType,
          long localAs,
          Ip localIp,
          Ip originalRouteNhip,
          @Nullable Integer pathId,
          boolean replaceAllAsesWithLocalAs) {
    // if eBGP, prepend as-path sender's as-path number
    if (isEbgp) {
      // TODO: Support more exotic prepending, e.g.:
      //       - On FRR, can prepend both ('router bgp' ASN/confed ASN) as well as 'local-as'
      //         override ASN, or just the local AS:
      // https://docs.frrouting.org/en/latest/bgp.html#clicmd-neighbor-PEER-local-as-AS-NUMBER-no-prepend-replace-as
      //       - On Juniper, can choose between prepending 'routing-options autonomous-system' ASN
      //         or 'local-as' ASN, or both
      // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/ref/statement/local-as-edit-protocols-bgp.html
      // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/topic-map/autonomous-systems.html#id-understanding-the-bgp-local-as-attribute
      // TODO: verify behavior of various non-FRR Cisco-like implementations of 'local-as'
      AsSet asSetToPrepend =
          confedSessionType == ConfedSessionType.WITHIN_CONFED
              ? AsSet.confed(localAs)
              : AsSet.of(localAs);

      // Remove any confederations if propagating route outside of the confederation border
      AsPath routeAsPath = routeBuilder.getAsPath();
      if (confedSessionType.equals(ConfedSessionType.ACROSS_CONFED_BORDER)) {
        routeAsPath = routeAsPath.removeConfederations();
      }
      routeAsPath =
          AsPath.of(
              ImmutableList.<AsSet>builder()
                  .add(asSetToPrepend)
                  .addAll(routeAsPath.getAsSets())
                  .build());
      if (replaceAllAsesWithLocalAs) {
        // Hide non-proximal AS path details by replacing all AsSet elements with the local AS
        routeAsPath = AsPath.ofSingletonAsSets(Collections.nCopies(routeAsPath.size(), localAs));
      }

      routeBuilder.setAsPath(routeAsPath);
    }

    // Tags are non-transitive
    routeBuilder.setTag(null);

    // Only send communities that are supported
    if (!sendStandardCommunities) {
      // No standard: Extended or nothing.
      routeBuilder.setCommunities(
          sendExtendedCommunities
              ? routeBuilder.getCommunities().getExtendedCommunities()
              : ImmutableSet.of());
    } else if (!sendExtendedCommunities) {
      // Standard, not extended.
      routeBuilder.setCommunities(routeBuilder.getCommunities().getStandardCommunities());
    } // else preserve all communities as-is.

    // Skip setting our own next hop if it has already been set by the routing policy
    // TODO: When sending out a BGP route with a NextHopVtep, should that next hop be preserved?
    //  If so, this should step be skipped for such routes.
    // TODO: This next hop is incorrect for EVPN type 3 routes (not critical since type 3 routes'
    //  next hops have no function).
    if (!(routeBuilder instanceof EvpnType5Route.Builder)
        && routeBuilder.getNextHopIp().equals(UNSET_ROUTE_NEXT_HOP_IP)) {
      if (isEbgp) {
        routeBuilder.setNextHopIp(localIp);
      } else { // iBGP session
        /*
        Note: implementation of next-hop-self in the general case is delegated to routing
        policy.
        If original route has next-hop ip, preserve it. If not, set our own.
        */
        routeBuilder.setNextHopIp(
            originalRouteNhip.equals(UNSET_ROUTE_NEXT_HOP_IP) ? localIp : originalRouteNhip);
      }
    }

    routeBuilder.setPathId(pathId);

    /*
    Routes can be aggregate only when generated locally. When transiting across nodes they must be BGP or
    IBGP.

    This is a bit of a hack since we currently overload AGGREGATE to mean aggregate protocol (i.e., just
    like on Juniper) AND to mean "locally generated". :(
    */
    if (routeBuilder.getProtocol() == RoutingProtocol.AGGREGATE) {
      routeBuilder.setProtocol(isEbgp ? RoutingProtocol.BGP : RoutingProtocol.IBGP);
    }
  }

  /**
   * Sets next hop on EVPN type 5 route builder in preparation for export. Uses original route's
   * next hop unless it is {@link NextHopDiscard}, which indicates the original route was
   * originated; in this case the route builder is given a {@link NextHopVtep}.
   *
   * @return {@code true} if the next hop was successfully set. May be {@code false} if the export
   *     candidate was originated on this node and our EVPN address family has no NVE IP.
   */
  public static boolean setEvpnType5NhPostExport(
      EvpnType5Route.Builder routeBuilder,
      EvpnAddressFamily af,
      NextHop originalRouteNh,
      int originalRouteVni) {
    if (!originalRouteNh.equals(NextHopDiscard.instance())) {
      // Original route has a non-discard next hop. Use that.
      routeBuilder.setNextHop(originalRouteNh);
      return true;
    }
    // Original route has NextHopDiscard, so create a NextHopVtep for the exported route.
    if (af.getNveIp() == null) {
      // Can't create a NextHopVtep.
      return false;
    }
    routeBuilder.setNextHop(NextHopVtep.of(originalRouteVni, af.getNveIp()));
    return true;
  }

  private BgpProtocolHelper() {}
}
