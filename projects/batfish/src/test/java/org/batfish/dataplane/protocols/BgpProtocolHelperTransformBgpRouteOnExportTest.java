package org.batfish.dataplane.protocols;

import static org.batfish.common.util.BgpRouteUtil.convertNonBgpRouteToBgpRoute;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasOriginType;
import static org.batfish.dataplane.protocols.BgpProtocolHelper.convertGeneratedRouteToBgp;
import static org.batfish.dataplane.protocols.BgpProtocolHelper.setEvpnType5NhPostExport;
import static org.batfish.dataplane.protocols.BgpProtocolHelper.transformBgpRoutePostExport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.BgpTopologyUtils.ConfedSessionType;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.vendor.arista.representation.AristaConfiguration;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link BgpProtocolHelper#transformBgpRoutePreExport} */
public final class BgpProtocolHelperTransformBgpRouteOnExportTest {

  private final NetworkFactory _nf = new NetworkFactory();
  private BgpActivePeerConfig _fromNeighbor;
  private BgpPeerConfig _toNeighbor;
  private BgpSessionProperties _sessionProperties;
  private BgpProcess _fromBgpProcess;
  private BgpProcess _toBgpProcess;

  private GeneratedRoute.Builder _baseAggRouteBuilder;
  private Bgpv4Route.Builder _baseBgpRouteBuilder;

  /** AS for both source and dest peers if IBGP, or only source peer if EBGP */
  private static final Long AS1 = 1L;

  /** AS for dest peer if EBGP */
  private static final Long AS2 = 2L;

  private static final Ip DEST_IP = Ip.parse("3.3.3.3");
  private static final Prefix DEST_NETWORK = Prefix.parse("4.4.4.0/24");
  private static final Ip ORIGINATOR_IP = Ip.parse("1.1.1.1");
  private static final Ip SOURCE_IP = Ip.parse("2.2.2.2");

  /**
   * Sets aggregate and BGP route builders to the most basic form that will result in a route that
   * gets exported (given the default BGP session setup created in {@link #setUpPeers(boolean)}).
   */
  @Before
  public void resetDefaultRouteBuilders() {
    _baseAggRouteBuilder =
        GeneratedRoute.builder().setNextHop(NextHopDiscard.instance()).setNetwork(DEST_NETWORK);
    _baseBgpRouteBuilder =
        Bgpv4Route.testBuilder()
            .setOriginatorIp(ORIGINATOR_IP)
            .setOriginType(OriginType.IGP)
            .setNetwork(DEST_NETWORK)
            .setNextHopIp(DEST_IP)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFrom(ReceivedFromSelf.instance());
  }

  /**
   * Sets up the class variables to represent a BGP peer relationship. Then they may be used as
   * parameters to {@link BgpProtocolHelper#transformBgpRoutePreExport}.
   *
   * @param ibgp Whether to make the peer relationship IBGP
   */
  private void setUpPeers(boolean ibgp) {
    setUpPeers(ibgp, false);
  }

  /**
   * Sets up the class variables to represent a BGP peer relationship. Then they may be used as
   * parameters to {@link BgpProtocolHelper#transformBgpRoutePreExport}.
   *
   * @param ibgp Whether to make the peer relationship IBGP
   * @param replaceNonLocalAsesOnExport Whether the sender replaces non-local ASes in the AS with
   *     its local AS on export (only effective if {@code ibgp} is {@code false})
   */
  private void setUpPeers(boolean ibgp, boolean replaceNonLocalAsesOnExport) {
    Configuration c1 =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Configuration c2 =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    _fromNeighbor =
        _nf.bgpNeighborBuilder()
            .setLocalAs(AS1)
            .setRemoteAs(ibgp ? AS1 : AS2)
            .setLocalIp(SOURCE_IP)
            .setReplaceNonLocalAsesOnExport(replaceNonLocalAsesOnExport)
            .build();
    _toNeighbor =
        _nf.bgpNeighborBuilder()
            .setLocalAs(ibgp ? AS1 : AS2)
            .setRemoteAs(AS1)
            .setLocalIp(DEST_IP)
            .build();
    // These session props are used for export, so do not reverse direction.
    _sessionProperties = BgpSessionProperties.from(_fromNeighbor, _toNeighbor, false);
    Vrf fromVrf = _nf.vrfBuilder().setOwner(c1).build();
    fromVrf.setBgpProcess(BgpProcess.testBgpProcess(SOURCE_IP));
    _fromBgpProcess = fromVrf.getBgpProcess();
    Vrf toVrf = _nf.vrfBuilder().setOwner(c2).build();
    toVrf.setBgpProcess(BgpProcess.testBgpProcess(DEST_IP));
    _toBgpProcess = toVrf.getBgpProcess();
  }

  /**
   * Calls {@link BgpProtocolHelper#transformBgpRoutePreExport} with the given {@code route} and the
   * class variables representing the BGP session.
   */
  private Bgpv4Route.Builder runTransformBgpRoutePreExport(AbstractRoute route) {
    if (route instanceof GeneratedRoute) {
      return BgpProtocolHelper.transformBgpRoutePreExport(
          _fromNeighbor,
          _toNeighbor,
          _sessionProperties,
          _fromBgpProcess,
          _toBgpProcess,
          convertGeneratedRouteToBgp(
                  (GeneratedRoute) route,
                  _fromBgpProcess.getRouterId(),
                  NextHopIp.of(Objects.requireNonNull(_fromNeighbor.getLocalIp())),
                  false)
              .build(),
          Type.IPV4_UNICAST);
    } else if (route instanceof Bgpv4Route) {
      return BgpProtocolHelper.transformBgpRoutePreExport(
          _fromNeighbor,
          _toNeighbor,
          _sessionProperties,
          _fromBgpProcess,
          _toBgpProcess,
          (Bgpv4Route) route,
          Type.IPV4_UNICAST);
    } else {
      RoutingProtocol protocol =
          _sessionProperties.isEbgp() ? RoutingProtocol.BGP : RoutingProtocol.IBGP;
      return BgpProtocolHelper.transformBgpRoutePreExport(
          _fromNeighbor,
          _toNeighbor,
          _sessionProperties,
          _fromBgpProcess,
          _toBgpProcess,
          convertNonBgpRouteToBgpRoute(
                  route,
                  _fromBgpProcess.getRouterId(),
                  _sessionProperties.getLocalIp(),
                  protocol.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
                  protocol,
                  OriginMechanism.REDISTRIBUTE)
              .build(),
          Type.IPV4_UNICAST);
    }
  }

  /**
   * Calls {@link BgpProtocolHelper#transformBgpRoutePostExport} with the given {@code routeBuilder}
   * and the class variables representing the BGP session.
   */
  private void runTransformBgpRoutePostExport(Bgpv4Route.Builder routeBuilder) {
    transformBgpRoutePostExport(
        routeBuilder,
        _sessionProperties.isEbgp(),
        _fromNeighbor
            .getIpv4UnicastAddressFamily()
            .getAddressFamilyCapabilities()
            .getSendCommunity(),
        _fromNeighbor
            .getIpv4UnicastAddressFamily()
            .getAddressFamilyCapabilities()
            .getSendExtendedCommunity(),
        ConfedSessionType.NO_CONFED,
        _fromNeighbor.getLocalAs(),
        Ip.parse("1.1.1.1"),
        Ip.parse("1.1.1.1"),
        null,
        _sessionProperties.getReplaceNonLocalAsesOnExport());
  }

  /**
   * Proves that unmodified setup does export the base routes, so that when later tests show a route
   * does not get exported, we know it's due to modifications made in that test.
   */
  @Test
  public void testBaseRoutesGetExported() {
    for (boolean isIbgp : ImmutableList.of(false, true)) {
      setUpPeers(isIbgp);
      assertThat(runTransformBgpRoutePreExport(_baseAggRouteBuilder.build()), not(nullValue()));
      assertThat(runTransformBgpRoutePreExport(_baseBgpRouteBuilder.build()), not(nullValue()));
    }
  }

  @Test
  public void testConvertGeneratedToBgpHasNextHop() {
    NextHop nextHop = NextHopIp.of(Ip.parse("1.1.1.1"));
    assertThat(
        convertGeneratedRouteToBgp(_baseAggRouteBuilder.build(), Ip.ZERO, nextHop, false).build(),
        hasNextHop(nextHop));
  }

  @Test
  public void testConvertGeneratedToBgpPreservesOriginType() {
    NextHop nextHop = NextHopIp.of(Ip.parse("1.1.1.1"));
    assertThat(
        convertGeneratedRouteToBgp(
                _baseAggRouteBuilder.setOriginType(OriginType.IGP).build(), Ip.ZERO, nextHop, false)
            .build(),
        hasOriginType(OriginType.IGP));
    assertThat(
        convertGeneratedRouteToBgp(
                _baseAggRouteBuilder.setOriginType(OriginType.INCOMPLETE).build(),
                Ip.ZERO,
                nextHop,
                false)
            .build(),
        hasOriginType(OriginType.INCOMPLETE));
  }

  /**
   * Test that communities are correctly transferred to the transformed route if source peer has
   * sendCommunity set, in IBGP or EBGP.
   */
  @Test
  public void testCommunitiesInTransformedRoute() {
    for (boolean isIbgp : ImmutableList.of(false, true)) {
      setUpPeers(isIbgp);
      CommunitySet communities = CommunitySet.of(StandardCommunity.of(10L));
      Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setCommunities(communities).build();

      // By default, _fromNeighbor doesn't have sendCommunity set. They will appear int he
      Bgpv4Route.Builder beingExported = bgpv4Route.toBuilder();
      runTransformBgpRoutePostExport(beingExported);
      assertThat(beingExported, hasCommunities());

      // Now set sendCommunity and make sure communities appear in transformed routes.
      _fromNeighbor =
          _nf.bgpNeighborBuilder()
              .setIpv4UnicastAddressFamily(
                  Ipv4UnicastAddressFamily.builder()
                      .setAddressFamilyCapabilities(
                          AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                      .build())
              .setLocalAs(AS1)
              .setLocalIp(_fromNeighbor.getLocalIp())
              .setRemoteAsns(_fromNeighbor.getRemoteAsns())
              .build();
      beingExported = bgpv4Route.toBuilder();
      runTransformBgpRoutePostExport(beingExported);
      assertThat(beingExported, hasCommunities(communities));
    }
  }

  /**
   * Test that non-local ASes in the AS path are replaced with the local AS on export for eBGP if
   * that behavior is configured.
   */
  @Test
  public void testReplaceAllAsesWithLocalAs_true_ebgp() {
    setUpPeers(false, true);
    AsPath preExportAsPath = AsPath.ofSingletonAsSets(5L);
    Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setAsPath(preExportAsPath).build();

    Bgpv4Route.Builder beingExported = bgpv4Route.toBuilder();
    runTransformBgpRoutePostExport(beingExported);
    assertThat(beingExported, hasAsPath(equalTo(AsPath.ofSingletonAsSets(AS1, AS1))));
  }

  /**
   * Test that non-local ASes in the AS path are NOT replaced with the local AS on export for eBGP
   * if that behavior is NOT configured.
   */
  @Test
  public void testReplaceAllAsesWithLocalAs_false_ebgp() {
    setUpPeers(false, false);
    AsPath preExportAsPath = AsPath.ofSingletonAsSets(5L);
    Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setAsPath(preExportAsPath).build();

    Bgpv4Route.Builder beingExported = bgpv4Route.toBuilder();
    runTransformBgpRoutePostExport(beingExported);
    assertThat(beingExported, hasAsPath(equalTo(AsPath.ofSingletonAsSets(AS1, 5L))));
  }

  /**
   * Test that non-local ASes in the AS path are replaced with the local AS on export for eBGP if
   * that behavior is configured.
   */
  @Test
  public void testReplaceAllAsesWithLocalAs_ibgp() {
    for (boolean replaceNonLocalAsesOnExport : ImmutableList.of(false, true)) {
      setUpPeers(true, replaceNonLocalAsesOnExport);
      AsPath preExportAsPath = AsPath.ofSingletonAsSets(5L);
      Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setAsPath(preExportAsPath).build();

      Bgpv4Route.Builder beingExported = bgpv4Route.toBuilder();
      runTransformBgpRoutePostExport(beingExported);
      assertThat(beingExported, hasAsPath(equalTo(AsPath.ofSingletonAsSets(5L))));
    }
  }

  /**
   * Test that transformBgpRouteOnExport returns {@code null} (meaning do not export the route) if
   * it has the {@value org.batfish.common.WellKnownCommunity#NO_ADVERTISE} community (IBGP or EBGP)
   */
  @Test
  public void testRoutesWithNoAdvertiseSetNotExported() {
    for (boolean isIbgp : ImmutableList.of(false, true)) {
      setUpPeers(isIbgp);
      CommunitySet noAdvertiseCommunitySet = CommunitySet.of(StandardCommunity.NO_ADVERTISE);

      Bgpv4Route.Builder transformedAggregateRoute =
          runTransformBgpRoutePreExport(
              _baseAggRouteBuilder.setCommunities(noAdvertiseCommunitySet).build());
      Bgpv4Route.Builder transformedBgpRoute =
          runTransformBgpRoutePreExport(
              _baseBgpRouteBuilder.setCommunities(noAdvertiseCommunitySet).build());

      assertThat(transformedAggregateRoute, nullValue());
      assertThat(transformedBgpRoute, nullValue());
    }
  }

  /**
   * Test that transformBgpRouteOnExport returns {@code null} (meaning do not export the route) if
   * it has the {@value org.batfish.common.WellKnownCommunity#NO_EXPORT} community and the session
   * is EBGP.
   */
  @Test
  public void testRoutesWithNoExportSetNotExported() {
    CommunitySet noExportCommunitySet = CommunitySet.of(StandardCommunity.NO_EXPORT);

    {
      // iBGP
      setUpPeers(true);
      Bgpv4Route.Builder transformedAggregateRoute =
          runTransformBgpRoutePreExport(
              _baseAggRouteBuilder.setCommunities(noExportCommunitySet).build());
      Bgpv4Route.Builder transformedBgpRoute =
          runTransformBgpRoutePreExport(
              _baseBgpRouteBuilder.setCommunities(noExportCommunitySet).build());
      assertThat(transformedAggregateRoute, notNullValue());
      assertThat(transformedBgpRoute, notNullValue());
    }

    {
      // eBGP
      setUpPeers(false);
      Bgpv4Route.Builder transformedAggregateRoute =
          runTransformBgpRoutePreExport(
              _baseAggRouteBuilder.setCommunities(noExportCommunitySet).build());
      Bgpv4Route.Builder transformedBgpRoute =
          runTransformBgpRoutePreExport(
              _baseBgpRouteBuilder.setCommunities(noExportCommunitySet).build());
      assertThat(transformedAggregateRoute, nullValue());
      assertThat(transformedBgpRoute, nullValue());
    }
    {
      // eBGP within confederation
      _sessionProperties =
          BgpSessionProperties.builder()
              .setLocalIp(_toNeighbor.getLocalIp())
              .setLocalAs(_toNeighbor.getLocalAs())
              .setRemoteAs(_fromNeighbor.getLocalAs())
              .setRemoteIp(_fromNeighbor.getLocalIp())
              .setConfedSessionType(ConfedSessionType.WITHIN_CONFED)
              .build();
      Bgpv4Route.Builder transformedAggregateRoute =
          runTransformBgpRoutePreExport(
              _baseAggRouteBuilder.setCommunities(noExportCommunitySet).build());
      Bgpv4Route.Builder transformedBgpRoute =
          runTransformBgpRoutePreExport(
              _baseBgpRouteBuilder.setCommunities(noExportCommunitySet).build());
      assertThat(transformedAggregateRoute, notNullValue());
      assertThat(transformedBgpRoute, notNullValue());
    }
  }

  /**
   * Test that AS path is correctly transferred to the transformed route (not testing for IBGP
   * because that is tested in {@link #testEbgpDoesNotExportWithAsLoop()})
   */
  @Test
  public void testAsPathInTransformedRoute() {
    setUpPeers(false);
    long asInPath = 3L;
    AsPath originalAsPath = AsPath.of(AsSet.of(asInPath));
    Bgpv4Route.Builder transformedAggregateRoute =
        runTransformBgpRoutePreExport(_baseAggRouteBuilder.setAsPath(originalAsPath).build());
    Bgpv4Route.Builder transformedBgpRoute =
        runTransformBgpRoutePreExport(_baseBgpRouteBuilder.setAsPath(originalAsPath).build());

    // Expect correct as-path before export policy is applied
    AsPath expectedAsPathPreExport = AsPath.of(ImmutableList.of(AsSet.of(asInPath)));
    assertThat(transformedAggregateRoute.getAsPath(), equalTo(expectedAsPathPreExport));
    assertThat(transformedBgpRoute.getAsPath(), equalTo(expectedAsPathPreExport));

    // Expect final transformed route to have source peer's AS prepended to its AS path (since in
    // EBGP)
    AsPath expectedAsPathPostExport =
        AsPath.of(ImmutableList.of(AsSet.of(AS1), AsSet.of(asInPath)));
    runTransformBgpRoutePostExport(transformedAggregateRoute);
    runTransformBgpRoutePostExport(transformedBgpRoute);
    assertThat(transformedAggregateRoute.getAsPath(), equalTo(expectedAsPathPostExport));
    assertThat(transformedBgpRoute.getAsPath(), equalTo(expectedAsPathPostExport));

    // Also check for a route type that does not have an asPath property
    StaticRoute staticRoute =
        StaticRoute.testBuilder().setNetwork(DEST_NETWORK).setAdministrativeCost(1).build();
    Bgpv4Route.Builder transformedRoute = runTransformBgpRoutePreExport(staticRoute);
    assertThat(transformedRoute.getAsPath(), equalTo(AsPath.empty()));

    runTransformBgpRoutePostExport(transformedRoute);
    assertThat(transformedRoute.getAsPath(), equalTo(AsPath.of(AsSet.of(AS1))));
  }

  /**
   * Test that a route whose AS path contains the destination peer's AS is exported in IBGP but not
   * in EBGP (when source peer hasn't set allowRemoteAsOut).
   */
  @Test
  public void testEbgpDoesNotExportWithAsLoop() {
    AsPath asPathContainingDestPeer = AsPath.of(AsSet.of(AS2));
    Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setAsPath(asPathContainingDestPeer).build();
    GeneratedRoute aggRoute = _baseAggRouteBuilder.setAsPath(asPathContainingDestPeer).build();

    // IBGP: Routes should get exported with the expected AS path
    setUpPeers(true);
    Bgpv4Route.Builder transformedAggregateRoute = runTransformBgpRoutePreExport(aggRoute);
    Bgpv4Route.Builder transformedBgpRoute = runTransformBgpRoutePreExport(bgpv4Route);
    assertThat(transformedAggregateRoute.getAsPath(), equalTo(asPathContainingDestPeer));
    assertThat(transformedBgpRoute.getAsPath(), equalTo(asPathContainingDestPeer));

    // EBGP: Routes shouldn't get exported
    setUpPeers(false);
    transformedAggregateRoute = runTransformBgpRoutePreExport(aggRoute);
    transformedBgpRoute = runTransformBgpRoutePreExport(bgpv4Route);
    assertThat(transformedAggregateRoute, nullValue());
    assertThat(transformedBgpRoute, nullValue());
  }

  /** Test that MED is cleared on export to EBGP peers except for locally originated routes. */
  @Test
  public void testEbgpDoesNotExportWithMEDSet() {
    // _baseBgpRouteBuilder has receivedFromIp 0.0.0.0 indicating local origination
    Bgpv4Route locallyOriginated = _baseBgpRouteBuilder.setMetric(1000).build();
    Bgpv4Route notLocallyOriginated =
        locallyOriginated.toBuilder()
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .build();

    setUpPeers(false);
    assertThat(runTransformBgpRoutePreExport(locallyOriginated).getMetric(), equalTo(1000L));
    assertThat(runTransformBgpRoutePreExport(notLocallyOriginated).getMetric(), equalTo(0L));
  }

  /** Test that MED is preserved/advertised to IBGP peers. */
  @Test
  public void testIbgpDoesNotExportWithMEDSet() {
    Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setMetric(1000).build();

    setUpPeers(true);
    Bgpv4Route.Builder transformedBgpRoute = runTransformBgpRoutePreExport(bgpv4Route);
    assertThat(transformedBgpRoute.getMetric(), equalTo(1000L));
  }

  /** Test that weight is cleared before exporting for both IBGP and EBGP. */
  @Test
  public void testWeightIsClearedBeforeExport() {
    Bgpv4Route bgpv4Route = _baseBgpRouteBuilder.setWeight(19).build();

    // IBGP
    setUpPeers(true);
    Bgpv4Route.Builder transformedBgpRoute = runTransformBgpRoutePreExport(bgpv4Route);
    assertThat(transformedBgpRoute.getWeight(), equalTo(0));

    // EBGP
    setUpPeers(false);
    transformedBgpRoute = runTransformBgpRoutePreExport(bgpv4Route);
    assertThat(transformedBgpRoute.getWeight(), equalTo(0));
  }

  @Test
  public void testNonBgpToBgpKeepTag() {
    long tag = 100L;
    setUpPeers(false);
    assertThat(
        convertNonBgpRouteToBgpRoute(
                StaticRoute.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopInterface("foo")
                    .setAdministrativeCost(1)
                    .setTag(tag)
                    .build(),
                _fromBgpProcess.getRouterId(),
                _sessionProperties.getLocalIp(),
                170,
                RoutingProtocol.BGP,
                OriginMechanism.REDISTRIBUTE)
            .getTag(),
        equalTo(tag));
  }

  @Test
  public void testNonBgpToBgpKeepMetric() {
    setUpPeers(false);
    long metric = 333;
    assertThat(
        convertNonBgpRouteToBgpRoute(
                StaticRoute.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopInterface("foo")
                    .setAdministrativeCost(1)
                    .setMetric(metric)
                    .build(),
                _fromBgpProcess.getRouterId(),
                _sessionProperties.getLocalIp(),
                170,
                RoutingProtocol.BGP,
                OriginMechanism.REDISTRIBUTE)
            .getMetric(),
        equalTo(metric));
  }

  @Test
  public void testTransformBgpRoutePreExportKeepMetric_ibgp() {
    // Peers are IBGP, so routes should be exported with their metrics preserved
    setUpPeers(true);
    long metric = 333;
    Bgpv4Route bgpRoute =
        Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).setMetric(metric).build();
    assertThat(runTransformBgpRoutePreExport(bgpRoute).getMetric(), equalTo(metric));
  }

  @Test
  public void testAggregateProtocolIsCleared() {
    Builder routeBuilder = Bgpv4Route.testBuilder().setProtocol(RoutingProtocol.AGGREGATE);
    transformBgpRoutePostExport(
        routeBuilder,
        true,
        false,
        false,
        ConfedSessionType.NO_CONFED,
        1,
        Ip.parse("1.1.1.1"),
        Ip.ZERO,
        null,
        false);
    assertThat(
        "Protocol overriden to BGP", routeBuilder.getProtocol(), equalTo(RoutingProtocol.BGP));
  }

  @Test
  public void testSetEvpnType5NhPostExport() {
    // Simulate exporting an EVPN route. If the route appears to be originated (based on NHIP),
    // transformBgpRoutePostExport should set its NHIP to the address family's NVE IP. Otherwise, it
    // should preserve the original NHIP for both EBGP and IBGP.
    int exportRouteVni = 5;
    Ip nveIp = Ip.parse("10.10.10.10");
    EvpnAddressFamily.Builder afb =
        EvpnAddressFamily.builder()
            .setAddressFamilyCapabilities(AddressFamilyCapabilities.builder().build())
            .setPropagateUnmatched(false);
    EvpnAddressFamily afNoNveIp = afb.build();
    EvpnAddressFamily af = afb.setNveIp(nveIp).build();
    EvpnType5Route.Builder outgoingRouteBuilder =
        EvpnType5Route.builder()
            .setNetwork(Prefix.ZERO)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 15001))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(REDISTRIBUTE)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.CONNECTED)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .setVni(exportRouteVni)
            .setWeight(AristaConfiguration.DEFAULT_LOCAL_BGP_WEIGHT);
    {
      // Learned EVPN route: Exported route should keep original NH.
      // Whether address family has an NVE IP should not matter.
      NextHop originalNh = NextHopVtep.of(exportRouteVni, Ip.parse("5.5.5.5"));
      outgoingRouteBuilder.setProtocol(RoutingProtocol.BGP).clearNextHop();
      setUpPeers(false);
      assertTrue(setEvpnType5NhPostExport(outgoingRouteBuilder, af, originalNh, exportRouteVni));
      assertThat(outgoingRouteBuilder.build(), hasNextHop(originalNh));

      outgoingRouteBuilder.setProtocol(RoutingProtocol.BGP).clearNextHop();
      setUpPeers(false);
      assertTrue(
          setEvpnType5NhPostExport(outgoingRouteBuilder, afNoNveIp, originalNh, exportRouteVni));
      assertThat(outgoingRouteBuilder.build(), hasNextHop(originalNh));
    }
    {
      // Originated EVPN route: Exported route should use NextHopVtep.
      // Should succeed only if address family has an NVE IP.
      NextHop nhDiscard = NextHopDiscard.instance();
      outgoingRouteBuilder.setProtocol(RoutingProtocol.BGP).clearNextHop();
      setUpPeers(false);
      assertTrue(setEvpnType5NhPostExport(outgoingRouteBuilder, af, nhDiscard, exportRouteVni));
      assertThat(outgoingRouteBuilder.build(), hasNextHop(NextHopVtep.of(exportRouteVni, nveIp)));

      outgoingRouteBuilder.setProtocol(RoutingProtocol.BGP).clearNextHop();
      setUpPeers(false);
      assertFalse(
          setEvpnType5NhPostExport(outgoingRouteBuilder, afNoNveIp, nhDiscard, exportRouteVni));
    }
  }
}
