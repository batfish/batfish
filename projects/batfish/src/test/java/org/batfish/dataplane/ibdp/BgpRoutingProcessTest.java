package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;
import static org.batfish.datamodel.BumTransportMethod.UNICAST_FLOOD_GROUP;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.OriginType.IGP;
import static org.batfish.datamodel.OriginType.INCOMPLETE;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasAdministrativeCost;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasTag;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.isNonRouting;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasOriginType;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasWeight;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isBgpv4RouteThat;
import static org.batfish.datamodel.vxlan.Layer2Vni.testBuilder;
import static org.batfish.dataplane.ibdp.BgpRoutingProcess.evpnRouteToBgpv4Route;
import static org.batfish.dataplane.ibdp.BgpRoutingProcess.initEvpnType3Route;
import static org.batfish.dataplane.ibdp.BgpRoutingProcess.processExternalBgpAdvertisementImport;
import static org.batfish.dataplane.ibdp.BgpRoutingProcess.toEvpnType5Route;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpVrfLeakConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.bgp.BgpTopology.EdgeId;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Layer2VniConfig;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.VniConfig;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.dataplane.rib.Rib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link BgpRoutingProcess} */
public class BgpRoutingProcessTest {

  private Configuration _c;
  private Vrf _vrf;
  private Vrf _vrf2;
  private BgpProcess _bgpProcess;
  private BgpProcess _bgpProcess2;
  private BgpRoutingProcess _routingProcess;
  private NetworkFactory _nf;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _c =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c1")
            .build();
    _vrf = _nf.vrfBuilder().setOwner(_c).setName(DEFAULT_VRF_NAME).build();
    _vrf2 = _nf.vrfBuilder().setOwner(_c).setName("vrf2").build();
    _bgpProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    _bgpProcess2 = BgpProcess.testBgpProcess(Ip.ZERO);
    _vrf.setBgpProcess(_bgpProcess);
    _vrf2.setBgpProcess(_bgpProcess2);
    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());
  }

  @Test
  public void testInitRibsEmpty() {
    // iBGP
    assertThat(_routingProcess._ibgpv4Rib.getUnannotatedRoutes(), empty());
    // eBGP
    assertThat(_routingProcess._ebgpv4Rib.getUnannotatedRoutes(), empty());
    // Combined bgp (both eBGP and iBGP)
    assertThat(_routingProcess._bgpv4RibEbgp.getUnannotatedRoutes(), empty());
    assertThat(_routingProcess._bgpv4RibIbgp.getUnannotatedRoutes(), empty());
  }

  @Test
  public void testInitEvpnType3Route() {
    Ip ip = Ip.parse("1.1.1.1");
    ExtendedCommunity routeTarget = ExtendedCommunity.target(1, 1);
    RouteDistinguisher routeDistinguisher = RouteDistinguisher.from(ip, 1);
    int vni = 10001;
    EvpnType3Route route =
        initEvpnType3Route(
            Layer2Vni.testBuilder()
                .setVlan(1)
                .setVni(vni)
                .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                .setSourceAddress(ip)
                .build(),
            routeTarget,
            routeDistinguisher,
            ip);
    assertThat(
        route,
        equalTo(
            EvpnType3Route.builder()
                .setRouteDistinguisher(routeDistinguisher)
                .setCommunities(ImmutableSet.of(routeTarget))
                .setVni(vni)
                .setProtocol(RoutingProtocol.BGP)
                .setOriginMechanism(OriginMechanism.GENERATED)
                .setOriginType(OriginType.IGP)
                .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
                .setVniIp(ip)
                .setOriginatorIp(ip)
                .setNextHop(NextHopDiscard.instance())
                .setReceivedFrom(ReceivedFromSelf.instance())
                .build()));
  }

  @Test
  public void testInitEvpnRoutes() {
    // Setup
    Ip localIp = Ip.parse("2.2.2.2");
    int vni = 10001;
    int vni2 = 10002;
    Layer2VniConfig.Builder vniConfigBuilder =
        Layer2VniConfig.builder()
            .setVni(vni)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(_bgpProcess.getRouterId(), 1))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .setImportRouteTarget(VniConfig.importRtPatternForAnyAs(vni));
    Layer2VniConfig vniConfig1 = vniConfigBuilder.build();
    Layer2VniConfig vniConfig2 =
        vniConfigBuilder
            .setVni(vni2)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(_bgpProcess.getRouterId(), 2))
            .setRouteTarget(ExtendedCommunity.target(65500, vni2))
            .build();
    BgpActivePeerConfig evpnPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(localIp)
            .setLocalAs(2L)
            .setEvpnAddressFamily(
                EvpnAddressFamily.builder()
                    .setL2Vnis(ImmutableSet.of(vniConfig1, vniConfig2))
                    .setL3Vnis(ImmutableSet.of())
                    .setPropagateUnmatched(true)
                    .build())
            .build();
    _bgpProcess.getActiveNeighbors().put(localIp, evpnPeer);
    _vrf.addLayer2Vni(
        testBuilder()
            .setVni(vni)
            .setVlan(1)
            .setBumTransportMethod(UNICAST_FLOOD_GROUP)
            .setSourceAddress(localIp)
            .build());
    _vrf.addLayer2Vni(
        testBuilder()
            .setVni(vni2)
            .setVlan(2)
            .setBumTransportMethod(UNICAST_FLOOD_GROUP)
            .setSourceAddress(localIp)
            .build());

    Node node = new Node(_c);
    BgpRoutingProcess defaultProc =
        node.getVirtualRouterOrThrow(DEFAULT_VRF_NAME).getBgpRoutingProcess();

    // Test
    defaultProc.initLocalEvpnRoutes();

    // The VRF/process that the neighbor is in
    assertThat(
        defaultProc.getEvpnType3Routes(),
        containsInAnyOrder(
            EvpnType3Route.builder()
                .setVniIp(localIp)
                .setRouteDistinguisher(RouteDistinguisher.from(_bgpProcess.getRouterId(), 1))
                .setCommunities(ImmutableSet.of(ExtendedCommunity.target(65500, vni)))
                .setVni(vni)
                .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
                .setOriginMechanism(OriginMechanism.GENERATED)
                .setOriginType(OriginType.IGP)
                .setProtocol(RoutingProtocol.BGP)
                .setOriginatorIp(_bgpProcess.getRouterId())
                .setReceivedFrom(ReceivedFromSelf.instance())
                .setNextHop(NextHopDiscard.instance())
                .build(),
            EvpnType3Route.builder()
                .setVniIp(localIp)
                .setRouteDistinguisher(RouteDistinguisher.from(_bgpProcess.getRouterId(), 2))
                .setCommunities(ImmutableSet.of(ExtendedCommunity.target(65500, vni2)))
                .setVni(vni2)
                .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
                .setOriginMechanism(OriginMechanism.GENERATED)
                .setOriginType(OriginType.IGP)
                .setReceivedFrom(ReceivedFromSelf.instance())
                .setProtocol(RoutingProtocol.BGP)
                .setOriginatorIp(_bgpProcess.getRouterId())
                .setNextHop(NextHopDiscard.instance())
                .build()));
  }

  @Test
  public void testQueueInitializationAddressFamiliesMustOverlap() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    long localAs = 1;
    long remoteAs = 1;
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(localAs)
            .setRemoteAs(remoteAs)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    BgpProcess bgpProc = BgpProcess.testBgpProcess(Ip.ZERO);
    bgpProc.setNeighbors(ImmutableSortedMap.of(ip1, peer1));
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().build();

    BgpPeerConfigId peer1Id =
        new BgpPeerConfigId(_c.getHostname(), DEFAULT_VRF_NAME, ip1.toPrefix(), false);
    BgpPeerConfigId peer2Id =
        new BgpPeerConfigId("someHost", DEFAULT_VRF_NAME, ip1.toPrefix(), false);
    BgpSessionProperties.Builder sessionBuilderForward =
        BgpSessionProperties.builder()
            .setRemoteAs(remoteAs)
            .setLocalAs(localAs)
            .setRemoteIp(ip2)
            .setLocalIp(ip1)
            .setAddressFamilies(ImmutableSet.of(Type.EVPN));
    BgpSessionProperties.Builder sessionBuilderReverse =
        BgpSessionProperties.builder()
            .setRemoteAs(localAs)
            .setLocalAs(remoteAs)
            .setRemoteIp(ip1)
            .setLocalIp(ip2)
            .setAddressFamilies(ImmutableSet.of(Type.EVPN));
    graph.putEdgeValue(peer1Id, peer2Id, sessionBuilderForward.build());
    graph.putEdgeValue(peer2Id, peer1Id, sessionBuilderReverse.build());

    BgpTopology topology = new BgpTopology(graph);
    BgpRoutingProcess routingProcess =
        new BgpRoutingProcess(
            bgpProc, _c, DEFAULT_VRF_NAME, new Rib(), topology, new PrefixTracer());

    // No compatible peers for IPv4
    assertThat(
        routingProcess
            .getEdgeIdStream(graph, BgpPeerConfig::getIpv4UnicastAddressFamily, Type.IPV4_UNICAST)
            .collect(Collectors.toSet()),
        empty());

    // Replace session to include IPv4 AF. Let routingProcess know we made the new topology
    graph.putEdgeValue(
        peer1Id,
        peer2Id,
        sessionBuilderForward.setAddressFamilies(ImmutableSet.of(Type.IPV4_UNICAST)).build());
    graph.putEdgeValue(
        peer2Id,
        peer1Id,
        sessionBuilderReverse.setAddressFamilies(ImmutableSet.of(Type.IPV4_UNICAST)).build());
    topology = new BgpTopology(graph);
    routingProcess.updateTopology(topology);

    assertThat(
        routingProcess
            .getEdgeIdStream(graph, BgpPeerConfig::getIpv4UnicastAddressFamily, Type.IPV4_UNICAST)
            .collect(Collectors.toSet()),
        contains(new EdgeId(peer2Id, peer1Id)));
  }

  /** Ensure that we send out EVPN type 3 routes to newly established BGP sessions */
  @Test
  public void testResendInitializationOnTopologyUpdate() {
    // Setup
    long localAs = 2;
    long peerAs = 1;
    Ip localIp = Ip.parse("2.2.2.2");
    Ip peerIp = Ip.parse("1.1.1.1");
    int vni = 10001;
    Layer2VniConfig.Builder vniConfigBuilder =
        Layer2VniConfig.builder()
            .setVni(vni)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(_bgpProcess.getRouterId(), 2))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .setImportRouteTarget(VniConfig.importRtPatternForAnyAs(vni));
    Layer2VniConfig vniConfig1 = vniConfigBuilder.build();
    String policyName = "POL";
    BgpActivePeerConfig evpnPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(peerIp)
            .setRemoteAs(peerAs)
            .setLocalIp(localIp)
            .setLocalAs(localAs)
            .setEvpnAddressFamily(
                EvpnAddressFamily.builder()
                    .setL2Vnis(ImmutableSet.of(vniConfig1))
                    .setL3Vnis(ImmutableSet.of())
                    .setPropagateUnmatched(true)
                    .setExportPolicy(policyName)
                    .build())
            .build();
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(policyName)
        .setStatements(Collections.singletonList(Statements.ExitAccept.toStaticStatement()))
        .build();
    _bgpProcess.getActiveNeighbors().put(peerIp, evpnPeer);
    _vrf.addLayer2Vni(
        testBuilder()
            .setVni(vni)
            .setVlan(1)
            .setBumTransportMethod(UNICAST_FLOOD_GROUP)
            .setSourceAddress(localIp)
            .build());
    _vrf2.addLayer2Vni(
        testBuilder()
            .setVni(vni)
            .setVlan(2)
            .setBumTransportMethod(UNICAST_FLOOD_GROUP)
            .setSourceAddress(localIp)
            .build());

    Node node = new Node(_c);
    BgpRoutingProcess routingProcNode1 =
        node.getVirtualRouterOrThrow(DEFAULT_VRF_NAME).getBgpRoutingProcess();

    /////////// Node 2

    Configuration c2 =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c2")
            .build();
    Vrf vrf2 = _nf.vrfBuilder().setOwner(c2).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgp2 = BgpProcess.testBgpProcess(Ip.MAX);
    vrf2.setBgpProcess(bgp2);
    BgpActivePeerConfig node2Peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(localIp)
            .setRemoteAs(localAs)
            .setLocalIp(peerIp)
            .setLocalAs(peerAs)
            .setEvpnAddressFamily(
                EvpnAddressFamily.builder()
                    .setL2Vnis(ImmutableSet.of())
                    .setL3Vnis(ImmutableSet.of())
                    .setPropagateUnmatched(true)
                    .build())
            .build();
    bgp2.getActiveNeighbors().put(localIp, node2Peer);
    Node node2 = new Node(c2);
    BgpRoutingProcess routingProcNode2 =
        node2.getVirtualRouterOrThrow(DEFAULT_VRF_NAME).getBgpRoutingProcess();
    routingProcNode2.initialize(node2);

    /*
    Test:
    1. initalize
    2. execute one iteration/clear initialization state
    3. Update topology
    4. Ensure BGP rib state (at least type 3 routes) are sent to new neighbors.
    */
    routingProcNode1.initialize(node);
    routingProcNode1.executeIteration(ImmutableMap.of());
    // Update topology
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().build();
    BgpPeerConfigId peer1Id =
        new BgpPeerConfigId(_c.getHostname(), DEFAULT_VRF_NAME, peerIp.toPrefix(), false);
    BgpPeerConfigId peer2Id =
        new BgpPeerConfigId("c2", DEFAULT_VRF_NAME, localIp.toPrefix(), false);
    BgpSessionProperties.Builder sessionBuilderForward =
        BgpSessionProperties.builder()
            .setRemoteAs(localAs)
            .setLocalAs(peerAs)
            .setRemoteIp(localIp)
            .setLocalIp(peerIp)
            .setAddressFamilies(ImmutableSet.of(Type.EVPN));
    BgpSessionProperties.Builder sessionBuilderReverse =
        BgpSessionProperties.builder()
            .setRemoteAs(peerAs)
            .setLocalAs(localAs)
            .setRemoteIp(peerIp)
            .setLocalIp(localIp)
            .setAddressFamilies(ImmutableSet.of(Type.EVPN));
    graph.putEdgeValue(
        peer1Id,
        peer2Id,
        sessionBuilderForward.setAddressFamilies(ImmutableSet.of(Type.EVPN)).build());
    graph.putEdgeValue(
        peer2Id,
        peer1Id,
        sessionBuilderReverse.setAddressFamilies(ImmutableSet.of(Type.EVPN)).build());
    routingProcNode1.updateTopology(new BgpTopology(graph));
    routingProcNode2.updateTopology(new BgpTopology(graph));
    routingProcNode1.executeIteration(
        ImmutableSortedMap.of(
            node.getConfiguration().getHostname(),
            node,
            node2.getConfiguration().getHostname(),
            node2));

    assertThat(
        routingProcNode2._evpnType3IncomingRoutes.get(new BgpTopology.EdgeId(peer1Id, peer2Id)),
        not(empty()));
  }

  /**
   * Check that redistribution does not affect local RIB if the redistribution policy is not
   * defined.
   */
  @Test
  public void testRedistributionNoPolicy() {
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build(), _vrf.getName())));
    assertThat(_routingProcess.getV4Routes(), empty());
  }

  /**
   * Check that redistribution affects the RIB if the redistribution policy is defined and allows
   * the route.
   */
  @Test
  public void testRedistributionWithPolicy() {
    // Make up a policy
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("redistribute_policy")
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.CONNECTED),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    // If BGP has a redistribution policy, config must export from BGP RIB
    _c.setExportBgpFromBgpRib(true);
    _bgpProcess.setRedistributionPolicy(policy.getName());
    // re-init routing process after modifying configuration.
    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());

    Prefix prefix = Prefix.parse("1.1.1.0/24");

    // Process denied route
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                StaticRoute.testBuilder().setNetwork(prefix).build(), _vrf.getName())));
    assertThat(_routingProcess.getV4Routes(), empty());

    // Fake up end of round before other test
    _routingProcess.endOfRound();

    // Process allowed route
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                ConnectedRoute.builder()
                    .setNetwork(prefix)
                    .setNextHop(NextHopInterface.of("foo"))
                    .build(),
                _vrf.getName())));
    assertThat(
        _routingProcess.getV4Routes(),
        contains(
            isBgpv4RouteThat(
                allOf(
                    hasNextHop(NextHopDiscard.instance()),
                    hasPrefix(prefix),
                    hasProtocol(RoutingProtocol.BGP),
                    isNonRouting(true)))));
  }

  /**
   * Check that redistribution installs two routes in the BGP RIB if both redistribution policy and
   * network policy are defined and allow the route. Assume the transformed routes are distinct.
   */
  @Test
  public void testRedistributionNetworkAndRedistributionPolicy() {
    Prefix prefix = Prefix.strict("10.0.0.0/24");
    // Policy redistributing by protocol
    RoutingPolicy policyProtocol =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("redistribute_connected")
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.CONNECTED),
                    ImmutableList.of(
                        new SetOrigin(new LiteralOrigin(IGP, null)),
                        Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    // Policy redistributing by network
    RoutingPolicy policyNetwork =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("network_10_0_0_0_24")
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(),
                        new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix)))),
                    ImmutableList.of(
                        new SetOrigin(new LiteralOrigin(INCOMPLETE, null)),
                        Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    // If BGP has a redistribution or network policy, config must export from BGP RIB
    _c.setExportBgpFromBgpRib(true);
    _bgpProcess.setRedistributionPolicy(policyProtocol.getName());
    _bgpProcess.setIndependentNetworkPolicy(policyNetwork.getName());
    // re-init routing process after modifying configuration.
    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());

    AnnotatedRoute<AbstractRoute> route =
        new AnnotatedRoute<>(
            ConnectedRoute.builder()
                .setNetwork(prefix)
                .setNextHop(NextHopInterface.of("foo"))
                .build(),
            _vrf.getName());

    _routingProcess.redistribute(RibDelta.adding(route));

    assertThat(
        _routingProcess.getV4Routes(),
        contains(
            isBgpv4RouteThat(
                allOf(
                    hasNextHop(NextHopDiscard.instance()),
                    hasPrefix(prefix),
                    hasProtocol(RoutingProtocol.BGP),
                    isNonRouting(true),
                    hasOriginType(IGP)))));
    assertThat(
        _routingProcess.getV4BackupRoutes(),
        containsInAnyOrder(
            isBgpv4RouteThat(
                allOf(
                    hasNextHop(NextHopDiscard.instance()),
                    hasPrefix(prefix),
                    hasProtocol(RoutingProtocol.BGP),
                    isNonRouting(true),
                    hasOriginType(IGP))),
            isBgpv4RouteThat(
                allOf(
                    hasNextHop(NextHopDiscard.instance()),
                    hasPrefix(prefix),
                    hasProtocol(RoutingProtocol.BGP),
                    isNonRouting(true),
                    hasOriginType(INCOMPLETE)))));
  }

  @Test
  public void testMainRibIndependentNetworkPolicy() {
    Prefix prefix1 = Prefix.strict("10.0.0.0/8");
    RoutingPolicy policyNetwork =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("main_rib_independent")
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(),
                        new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix1)))),
                    ImmutableList.of(
                        new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                        Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    // If BGP has a redistribution or network policy, config must export from BGP RIB
    _c.setExportBgpFromBgpRib(true);

    _bgpProcess.setMainRibIndependentNetworkPolicy(policyNetwork.getName());
    _bgpProcess.addUnconditionalNetworkStatements(prefix1);

    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());

    Node node = new Node(_c);
    _routingProcess.initialize(node);
    _routingProcess.executeIteration(ImmutableMap.of());
    // eBGP route should be in _bgpv4RibEbgp
    assertThat(
        _routingProcess._bgpv4RibEbgp.getUnannotatedRoutes(),
        contains(
            isBgpv4RouteThat(
                allOf(hasPrefix(prefix1), hasProtocol(RoutingProtocol.BGP), isNonRouting(true)))));
  }

  @Test
  public void testCrossVrfImport() {
    // Make up a policy
    Prefix allowedPrefix1 = Prefix.parse("1.1.1.0/24");
    Prefix allowedPrefix2 = Prefix.parse("1.1.1.0/25");
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("redistribute_policy")
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(),
                        new ExplicitPrefixSet(
                            new PrefixSpace(
                                new PrefixRange(allowedPrefix1, new SubRange(24, 32))))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    _bgpProcess.setRedistributionPolicy(policy.getName());
    // re-init routing process after modifying configuration.
    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());

    String otherVrf = "otherVrf";
    // Process denied prefix, specify policy
    Prefix deniedPrefix = Prefix.parse("2.2.2.0/24");
    ExtendedCommunity routeTarget = ExtendedCommunity.target(1, 1);
    _routingProcess.importCrossVrfV4Routes(
        Stream.of(
            RouteAdvertisement.adding(
                Bgpv4Route.testBuilder()
                    .setNetwork(deniedPrefix)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .build())),
        BgpVrfLeakConfig.builder()
            .setImportPolicy(policy.getName())
            .setImportFromVrf(otherVrf)
            .setAttachRouteTargets(routeTarget)
            .setAdmin(0)
            .setWeight(0)
            .build());
    assertThat(
        _routingProcess
            .getBgpv4DeltaBuilder()
            .build()
            .getRoutesStream()
            .collect(Collectors.toList()),
        empty());

    // Process allowed prefixes from IGP/BGP with policy
    int igpLeakAdmin = 6;
    int igpLeakWeight = 5;
    _routingProcess.importCrossVrfV4Routes(
        Stream.of(
            RouteAdvertisement.adding(
                Bgpv4Route.testBuilder()
                    .setNetwork(allowedPrefix1)
                    .setSrcProtocol(RoutingProtocol.OSPF)
                    .build()),
            RouteAdvertisement.adding(
                Bgpv4Route.testBuilder()
                    .setNetwork(allowedPrefix2)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .build())),
        BgpVrfLeakConfig.builder()
            .setImportPolicy(policy.getName())
            .setImportFromVrf(otherVrf)
            .setAdmin(igpLeakAdmin)
            .setAttachRouteTargets(routeTarget)
            .setWeight(igpLeakWeight)
            .build());
    assertThat(
        _routingProcess
            .getBgpv4DeltaBuilder()
            .build()
            .getRoutesStream()
            .collect(Collectors.toList()),
        containsInAnyOrder(
            isBgpv4RouteThat(
                allOf(
                    // leaked IGP route
                    hasAdministrativeCost(igpLeakAdmin),
                    hasWeight(igpLeakWeight),
                    hasPrefix(allowedPrefix1),
                    hasNextHop(NextHopVrf.of(otherVrf)),
                    isNonRouting(false),
                    hasCommunities(routeTarget))),
            isBgpv4RouteThat(
                allOf(
                    // leaked BGP route
                    hasAdministrativeCost(not(equalTo(igpLeakAdmin))),
                    hasWeight(not(equalTo(igpLeakWeight))),
                    hasPrefix(allowedPrefix2),
                    hasNextHop(NextHopVrf.of(otherVrf)),
                    isNonRouting(false),
                    hasCommunities(routeTarget)))));

    // Process denied prefix, but because no policy is specified, allow it
    _routingProcess.importCrossVrfV4Routes(
        Stream.of(
            RouteAdvertisement.adding(
                Bgpv4Route.testBuilder()
                    .setNetwork(deniedPrefix)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .build())),
        BgpVrfLeakConfig.builder()
            .setAttachRouteTargets(routeTarget)
            .setImportFromVrf(otherVrf)
            .setAdmin(0)
            .setWeight(0)
            .build());
    assertThat(
        _routingProcess
            .getBgpv4DeltaBuilder()
            .build()
            .getRoutesStream()
            .collect(Collectors.toList()),
        hasItem(
            isBgpv4RouteThat(
                allOf(
                    hasPrefix(deniedPrefix),
                    hasNextHop(NextHopVrf.of(otherVrf)),
                    isNonRouting(false),
                    hasCommunities(routeTarget)))));

    // Finally check that routes imported from other vrfs won't be exported for leaking.
    // Fake up end of round
    _routingProcess.endOfRound();
    assertThat(_routingProcess.getRoutesToLeak().collect(Collectors.toList()), empty());
  }

  /**
   * Test that potential contributors are funneled past most specific aggregate to more general
   * aggregates.
   */
  @Test
  public void testInitBgpAggregateRoutesFunnelContributors() {
    // Only allow the redistributed connected route to contribute to aggregate 1.0.0.0/16
    List<RouteAdvertisement<Bgpv4Route>> deltaAdverts = initBgpAggregatesTestHelper(32);

    // 1.0.0.0/16 should be activated by redistributed connected route 1.0.0.0/32 only
    assertThat(
        deltaAdverts.stream()
            .map(RouteAdvertisement::getRoute)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            hasPrefix(Prefix.strict("1.0.0.0/16")), hasPrefix(Prefix.strict("1.0.0.0/24"))));
  }

  /** Test that aggregates can contribute to more general aggregates. */
  @Test
  public void testInitBgpAggregateRoutesAllowAggregatesToContributeToOtherAggregates() {
    // Only allow the aggregate 1.0.0.0/24 to contribute to more general aggregate 1.0.0.0/16
    List<RouteAdvertisement<Bgpv4Route>> deltaAdverts = initBgpAggregatesTestHelper(24);

    // 1.0.0.0/16 should be activated by activated aggregate route 1.0.0.0/24
    assertThat(
        deltaAdverts.stream()
            .map(RouteAdvertisement::getRoute)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            hasPrefix(Prefix.strict("1.0.0.0/16")), hasPrefix(Prefix.strict("1.0.0.0/24"))));
  }

  /** Test that generation policy must pass for aggregate to be activated. */
  @Test
  public void testInitBgpAggregateRoutesMustPassGenerationPolicy() {
    // No route has prefix length 20, so the /16 should not be activated.
    List<RouteAdvertisement<Bgpv4Route>> deltaAdverts = initBgpAggregatesTestHelper(20);

    // 1.0.0.0/16 should not be activated
    assertThat(
        deltaAdverts.stream()
            .map(RouteAdvertisement::getRoute)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(hasPrefix((Prefix.strict("1.0.0.0/24")))));
  }

  /*
   * - Sets up a process with 2 aggregates: 1.0.0.0/16 and 1.0.0.0/24.
   * - Activation of 1.0.0.0/16 is controlled by prefixLengthContributingToAggregate16
   *   - only routes with provided prefix length will active it
   * - Redistributes connected route 1.0.0.0/32 into BGP
   * - Calls initBgpAggregates and returns the actions in resulting RIB delta.
   */
  private @Nonnull List<RouteAdvertisement<Bgpv4Route>> initBgpAggregatesTestHelper(
      int prefixLengthContributingToAggregate16) {
    // Setup
    _c.setExportBgpFromBgpRib(true);

    // Make up a redistribution policy
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("redistribute_policy")
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.CONNECTED),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    _bgpProcess.setRedistributionPolicy(policy.getName());

    // Create policy only allowing specific prefix lengths to contribute to /16 aggregate
    String filterByPrefixLength = "filterByPrefixLength";
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(filterByPrefixLength)
        .addStatement(
            new If(
                new MatchPrefixSet(
                    DestinationNetwork.instance(),
                    new ExplicitPrefixSet(
                        new PrefixSpace(
                            new PrefixRange(
                                Prefix.ZERO,
                                SubRange.singleton(prefixLengthContributingToAggregate16))))),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                ImmutableList.of(Statements.ExitReject.toStaticStatement())))
        .build();
    _bgpProcess.addAggregate(BgpAggregate.of(Prefix.strict("1.0.0.0/24"), null, null, null));
    _bgpProcess.addAggregate(
        BgpAggregate.of(Prefix.strict("1.0.0.0/16"), null, filterByPrefixLength, null));

    // re-init routing process after modifying configuration.
    _routingProcess =
        new BgpRoutingProcess(
            _bgpProcess, _c, DEFAULT_VRF_NAME, new Rib(), BgpTopology.EMPTY, new PrefixTracer());

    // Process allowed route
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                ConnectedRoute.builder()
                    .setNetwork(Prefix.strict("1.0.0.0/32"))
                    .setNextHop(NextHopInterface.of("foo"))
                    .build(),
                _vrf.getName())));

    // Test
    List<RouteAdvertisement<Bgpv4Route>> deltaAdverts =
        _routingProcess.initBgpAggregateRoutes().stream().collect(ImmutableList.toImmutableList());

    // Initially, there should only be adds.
    deltaAdverts.forEach(advert -> assertThat(advert.getReason(), equalTo(Reason.ADD)));
    return deltaAdverts;
  }

  @Test
  public void testProcessExternalBgpAdvertisementImport() {
    Ip neighborIp = Ip.parse("1.1.1.1");

    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setLocalIp(Ip.parse("1.1.1.2"))
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(neighborIp)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // Create a routing policy that sets next-hop peer-address, which relies on method under test
    // having created a BGP session properties object
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("policy")
            .setStatements(
                Collections.singletonList(new SetNextHop(BgpPeerAddressNextHop.getInstance())))
            .build();

    Bgpv4Route.Builder routeBuilder =
        Bgpv4Route.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(neighborIp)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(neighborIp))
            .setNextHopIp(Ip.parse("2.2.2.2"));

    Bgpv4Route inputRoute = routeBuilder.build();
    Bgpv4Route.Builder outputRouteBuilder = routeBuilder;

    processExternalBgpAdvertisementImport(inputRoute, outputRouteBuilder, peer, policy, null);

    // policy application will overwrite the next hop ip
    assertThat(outputRouteBuilder.build().getNextHopIp(), equalTo(neighborIp));
  }

  @Test
  public void testToEvpnType5Route() {
    // ensure tag is copied
    Bgpv4Route inputRoute = Bgpv4Route.testBuilder().setTag(5L).setNetwork(Prefix.ZERO).build();
    assertThat(
        toEvpnType5Route(inputRoute, RouteDistinguisher.from(Ip.ZERO, 1), ImmutableSet.of(), 1),
        hasTag(5L));
  }

  @Test
  public void testEvpnRouteToBgpv4Route() {
    // ensure tag is copied
    EvpnType5Route inputRoute =
        EvpnType5Route.builder()
            .setTag(5L)
            .setNetwork(Prefix.ZERO)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.ZERO, 1))
            .setVni(1)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            .build();
    assertThat(evpnRouteToBgpv4Route(inputRoute, 1).build(), hasTag(5L));
  }

  /**
   * Test that _bgpv4RibEbgp and _ebgpv4Rib process eBGP routes consistently when multipathIbgp is
   * enabled but multipathEbgp is disabled.
   *
   * <p>GitHub issue: https://github.com/batfish/batfish/issues/8990
   */
  @Test
  public void testBgpv4RibEbgpConsistencyWithEbgpv4Rib() {
    // Setup BGP process with multipathIbgp=true, multipathEbgp=false
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c1")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgpProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    bgpProcess.setMultipathIbgp(true);
    bgpProcess.setMultipathEbgp(false);
    vrf.setBgpProcess(bgpProcess);
    Rib mainRib = new Rib();
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("70.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("60.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));

    BgpRoutingProcess routingProcess =
        new BgpRoutingProcess(
            bgpProcess, c, DEFAULT_VRF_NAME, mainRib, BgpTopology.EMPTY, new PrefixTracer());

    // Create two equal-cost eBGP routes
    Prefix pfx = Prefix.parse("10.0.1.0/24");
    Bgpv4Route r1 =
        Bgpv4Route.testBuilder()
            .setNetwork(pfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();
    Bgpv4Route r2 =
        Bgpv4Route.testBuilder()
            .setNetwork(pfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();

    // Insert r1 into _bgpv4RibEbgp and _ebgpv4Rib
    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(r1);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(r1);
    // Both should have the same routes after inserting r1
    assertThat(
        routingProcess._bgpv4RibEbgp.getRoutes(pfx),
        equalTo(routingProcess._ebgpv4Rib.getRoutes(pfx)));

    // Insert r2 into _bgpv4RibEbgp and _ebgpv4Rib
    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(r2);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(r2);
    // Both should still have the same routes after inserting r2
    // (This was the bug: _bgpv4Rib would have both routes while _ebgpv4Rib would have only one)
    assertThat(
        "Combined eBGP RIB should have same routes as eBGP-only RIB when multipathEbgp=false",
        routingProcess._bgpv4RibEbgp.getRoutes(pfx),
        equalTo(routingProcess._ebgpv4Rib.getRoutes(pfx)));
  }

  /**
   * Test that _bgpv4RibIbgp and _ibgpv4Rib process iBGP routes consistently when multipathEbgp is
   * enabled but multipathIbgp is disabled (reverse of the original bug).
   */
  @Test
  public void testBgpv4RibIbgpConsistencyWithIbgpv4Rib() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c1")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgpProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    bgpProcess.setMultipathIbgp(false);
    bgpProcess.setMultipathEbgp(true);
    vrf.setBgpProcess(bgpProcess);
    Rib mainRib = new Rib();
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("70.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("60.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));

    BgpRoutingProcess routingProcess =
        new BgpRoutingProcess(
            bgpProcess, c, DEFAULT_VRF_NAME, mainRib, BgpTopology.EMPTY, new PrefixTracer());

    // Create two equal-cost iBGP routes
    Prefix pfx = Prefix.parse("10.0.1.0/24");
    Bgpv4Route r1 =
        Bgpv4Route.testBuilder()
            .setNetwork(pfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();
    Bgpv4Route r2 =
        Bgpv4Route.testBuilder()
            .setNetwork(pfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();

    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(r1);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(r1);
    assertThat(
        routingProcess._bgpv4RibIbgp.getRoutes(pfx),
        equalTo(routingProcess._ibgpv4Rib.getRoutes(pfx)));

    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(r2);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(r2);
    assertThat(
        "Combined iBGP RIB should have same routes as iBGP-only RIB when multipathIbgp=false",
        routingProcess._bgpv4RibIbgp.getRoutes(pfx),
        equalTo(routingProcess._ibgpv4Rib.getRoutes(pfx)));
  }

  /**
   * Test that when both multipath are enabled, both eBGP and iBGP routes allow multipath in their
   * respective combined RIBs.
   */
  @Test
  public void testBothMultipathEnabled() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c1")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgpProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    bgpProcess.setMultipathIbgp(true);
    bgpProcess.setMultipathEbgp(true);
    vrf.setBgpProcess(bgpProcess);
    Rib mainRib = new Rib();
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("70.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("60.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));

    BgpRoutingProcess routingProcess =
        new BgpRoutingProcess(
            bgpProcess, c, DEFAULT_VRF_NAME, mainRib, BgpTopology.EMPTY, new PrefixTracer());

    // Create two equal-cost eBGP routes
    Prefix ebgpPfx = Prefix.parse("10.0.1.0/24");
    Bgpv4Route ebgpR1 =
        Bgpv4Route.testBuilder()
            .setNetwork(ebgpPfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();
    Bgpv4Route ebgpR2 =
        Bgpv4Route.testBuilder()
            .setNetwork(ebgpPfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();

    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(ebgpR1);
    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(ebgpR2);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(ebgpR1);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(ebgpR2);
    // Both RIBs should have both routes (multipath enabled)
    assertThat(
        "Both RIBs should have 2 eBGP routes when multipathEbgp=true",
        routingProcess._bgpv4RibEbgp.getRoutes(ebgpPfx).size(),
        equalTo(2));
    assertThat(
        routingProcess._bgpv4RibEbgp.getRoutes(ebgpPfx),
        equalTo(routingProcess._ebgpv4Rib.getRoutes(ebgpPfx)));

    // Create two equal-cost iBGP routes
    Prefix ibgpPfx = Prefix.parse("10.0.2.0/24");
    Bgpv4Route ibgpR1 =
        Bgpv4Route.testBuilder()
            .setNetwork(ibgpPfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();
    Bgpv4Route ibgpR2 =
        Bgpv4Route.testBuilder()
            .setNetwork(ibgpPfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();

    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(ibgpR1);
    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(ibgpR2);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(ibgpR1);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(ibgpR2);
    // Both RIBs should have both routes (multipath enabled)
    assertThat(
        "Both RIBs should have 2 iBGP routes when multipathIbgp=true",
        routingProcess._bgpv4RibIbgp.getRoutes(ibgpPfx).size(),
        equalTo(2));
    assertThat(
        routingProcess._bgpv4RibIbgp.getRoutes(ibgpPfx),
        equalTo(routingProcess._ibgpv4Rib.getRoutes(ibgpPfx)));
  }

  /**
   * Test that when both multipath are disabled, neither eBGP nor iBGP routes allow multipath in
   * their combined RIBs.
   */
  @Test
  public void testBothMultipathDisabled() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c1")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgpProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    bgpProcess.setMultipathIbgp(false);
    bgpProcess.setMultipathEbgp(false);
    vrf.setBgpProcess(bgpProcess);
    Rib mainRib = new Rib();
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("70.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));
    mainRib.mergeRouteGetDelta(
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(Prefix.parse("60.0.0.0/24")).build(),
            DEFAULT_VRF_NAME));

    BgpRoutingProcess routingProcess =
        new BgpRoutingProcess(
            bgpProcess, c, DEFAULT_VRF_NAME, mainRib, BgpTopology.EMPTY, new PrefixTracer());

    // Create two equal-cost eBGP routes
    Prefix ebgpPfx = Prefix.parse("10.0.1.0/24");
    Bgpv4Route ebgpR1 =
        Bgpv4Route.testBuilder()
            .setNetwork(ebgpPfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();
    Bgpv4Route ebgpR2 =
        Bgpv4Route.testBuilder()
            .setNetwork(ebgpPfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .build();

    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(ebgpR1);
    routingProcess._bgpv4RibEbgp.mergeRouteGetDelta(ebgpR2);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(ebgpR1);
    routingProcess._ebgpv4Rib.mergeRouteGetDelta(ebgpR2);
    // Both RIBs should have only 1 route (multipath disabled)
    assertThat(
        "Both RIBs should have 1 eBGP route when multipathEbgp=false",
        routingProcess._bgpv4RibEbgp.getRoutes(ebgpPfx).size(),
        equalTo(1));
    assertThat(
        routingProcess._bgpv4RibEbgp.getRoutes(ebgpPfx),
        equalTo(routingProcess._ebgpv4Rib.getRoutes(ebgpPfx)));

    // Create two equal-cost iBGP routes
    Prefix ibgpPfx = Prefix.parse("10.0.2.0/24");
    Bgpv4Route ibgpR1 =
        Bgpv4Route.testBuilder()
            .setNetwork(ibgpPfx)
            .setNextHopIp(Ip.parse("70.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("70.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();
    Bgpv4Route ibgpR2 =
        Bgpv4Route.testBuilder()
            .setNetwork(ibgpPfx)
            .setNextHopIp(Ip.parse("60.0.0.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("60.0.0.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();

    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(ibgpR1);
    routingProcess._bgpv4RibIbgp.mergeRouteGetDelta(ibgpR2);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(ibgpR1);
    routingProcess._ibgpv4Rib.mergeRouteGetDelta(ibgpR2);
    // Both RIBs should have only 1 route (multipath disabled)
    assertThat(
        "Both RIBs should have 1 iBGP route when multipathIbgp=false",
        routingProcess._bgpv4RibIbgp.getRoutes(ibgpPfx).size(),
        equalTo(1));
    assertThat(
        routingProcess._bgpv4RibIbgp.getRoutes(ibgpPfx),
        equalTo(routingProcess._ibgpv4Rib.getRoutes(ibgpPfx)));
  }
}
