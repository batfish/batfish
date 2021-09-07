package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.BgpProcess.testBgpProcess;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GeneratedRoute.Builder;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.bgp.BgpTopologyUtils.BgpSessionInitiationResult;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link IncrementalDataPlanePlugin}. */
public class IncrementalDataPlanePluginTest {

  private static final String TESTRIGS_PREFIX = "org/batfish/dataplane/ibdp/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final String CORE_NAME = "core";
  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private BgpActivePeerConfig.Builder _nb;
  private Vrf.Builder _vb;
  private RoutingPolicy.Builder _epb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder();
    _ib = _nf.interfaceBuilder();
    _nb = _nf.bgpNeighborBuilder();
    _vb = _nf.vrfBuilder();
    _epb = _nf.routingPolicyBuilder();
  }

  @Test(timeout = 5000)
  public void testComputeFixedPoint() throws IOException {
    SortedMap<String, Configuration> configurations = new TreeMap<>();
    Configuration h1 =
        BatfishTestUtils.createTestConfiguration("h1", ConfigurationFormat.HOST, "eth0");
    configurations.put(h1.getHostname(), h1);
    Vrf vrf1 = Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(h1).build();
    h1.getAllInterfaces().get("eth0").setVrf(vrf1);

    Configuration h2 =
        BatfishTestUtils.createTestConfiguration("h2", ConfigurationFormat.HOST, "e0");
    configurations.put(h2.getHostname(), h2);
    Vrf vrf2 = Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(h2).build();
    h2.getAllInterfaces().get("e0").setVrf(vrf2);

    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    // Test that compute Data Plane finishes in a finite time
    dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
  }

  private SortedMap<String, Configuration> generateNetworkWithDuplicates() {
    Ip coreId = Ip.parse("1.1.1.1");
    Ip neighborId1 = Ip.parse("1.1.1.9");
    Ip neighborId2 = Ip.parse("1.1.1.2");
    final int interfacePrefixBits = 30;
    _vb.setName(DEFAULT_VRF_NAME);

    /*
     * Setup as follows:
     * 1.1.1.9               1.1.1.1            1.1.1.2
     * n1+---------x---------+core+---------------+n2
     * Core only has route to n2, because of interface masks
     */

    _epb.setStatements(ImmutableList.of(new SetDefaultPolicy("DEF")));
    Configuration core =
        _cb.setHostname(CORE_NAME).setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();

    Vrf corevrf = _vb.setOwner(core).build();
    _ib.setOwner(core).setVrf(corevrf);
    _ib.setAddress(ConcreteInterfaceAddress.create(coreId, interfacePrefixBits)).build();
    BgpProcess coreProc = testBgpProcess(coreId);
    corevrf.setBgpProcess(coreProc);
    _nb.setBgpProcess(coreProc)
        .setRemoteAs(1L)
        .setLocalAs(1L)
        .setLocalIp(coreId)
        .setPeerAddress(neighborId1)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(_epb.setOwner(core).build().getName())
                .build())
        .build();
    _nb.setRemoteAs(1L).setLocalAs(1L).setLocalIp(coreId).setPeerAddress(neighborId2).build();

    Configuration n1 = _cb.setHostname("n1").build();
    Vrf n1Vrf = _vb.setOwner(n1).build();
    _ib.setOwner(n1).setVrf(n1Vrf);
    _ib.setAddress(ConcreteInterfaceAddress.create(neighborId1, interfacePrefixBits)).build();
    BgpProcess n1Proc = testBgpProcess(neighborId1);
    n1Vrf.setBgpProcess(n1Proc);
    _nb.setBgpProcess(n1Proc)
        .setRemoteAs(1L)
        .setLocalAs(1L)
        .setLocalIp(neighborId1)
        .setPeerAddress(coreId)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(_epb.setOwner(n1).build().getName())
                .build())
        .build();

    Configuration n2 = _cb.setHostname("n2").build();
    Vrf n2Vrf = _vb.setOwner(n2).build();
    _ib.setOwner(n2).setVrf(n2Vrf);
    _ib.setAddress(ConcreteInterfaceAddress.create(neighborId2, interfacePrefixBits)).build();
    BgpProcess n2Proc = testBgpProcess(neighborId2);
    n2Vrf.setBgpProcess(n2Proc);
    _nb.setBgpProcess(n2Proc)
        .setRemoteAs(1L)
        .setLocalAs(1L)
        .setLocalIp(neighborId2)
        .setPeerAddress(coreId)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(_epb.setOwner(n2).build().getName())
                .build())
        .build();

    return ImmutableSortedMap.of(CORE_NAME, core, "n1", n1, "n2", n2);
  }

  @Test
  public void testBgpOscillation() throws IOException {
    String testrigName = "bgp-oscillation";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();

    // Really just test that no exception is thrown
    dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
  }

  @Test
  public void testEbgpAcceptSameNeighborID() throws IOException {
    String testrigName = "ebgp-accept-routerid-match";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    IncrementalDataPlanePlugin dataPlanePlugin = new IncrementalDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    ComputeDataPlaneResult dp = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp._dataPlane.getRibs();

    Set<AbstractRoute> r1Routes = ribs.get("r1").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(DEFAULT_VRF_NAME).getRoutes();
    Set<Prefix> r1Prefixes =
        r1Routes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes =
        r3Routes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = Prefix.parse("1.0.0.1/32");
    Prefix r3Loopback0Prefix = Prefix.parse("3.0.0.3/32");

    // Ensure that r3loopback was accepted by r1
    assertThat(r3Loopback0Prefix, in(r1Prefixes));
    // Check the other direction (r1loopback is accepted by r3)
    assertThat(r1Loopback0Prefix, in(r3Prefixes));
  }

  @Test
  public void testEbgpDynamicLocalIps() throws IOException {
    /*
     * Setup:       11.11.11.0/30       13.13.13.0/30
     *          r1 --------------- r2 ---------------- r3
     *
     * r1 and r3 have compatible BGP peers with peer addresses 13.13.13.1 and 11.11.11.1 respectively.
     * Neither peer has an update-source configured, but r1 and r3 both have static routes for their
     * respective peers' peer addresses:
     * - r1 has static route for 13.13.13.1/32 via 11.11.11.2
     * - r3 has static route for 11.11.11.1/32 via 13.13.13.2
     *
     * r1 and r3 also each have a static route to redistribute into BGP (21.21.21.21/32 and
     * 23.23.23.23/32, respectively) to help prove their BGP session is active.
     *
     * r2 is empty except for interfaces facing r1 and r3 (no BGP config).
     */
    String testrigName = "ebgp-dynamic-local-ips";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();

    Set<AbstractRoute> r1Routes = ribs.get("r1").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(DEFAULT_VRF_NAME).getRoutes();

    // r1 and r3's respective static routes were redistributed into BGP and reached each other
    Prefix r1StaticPrefix = Prefix.parse("21.21.21.21/32");
    Prefix r3StaticPrefix = Prefix.parse("23.23.23.23/32");
    assertThat(
        r1Routes, hasItem(allOf(hasProtocol(RoutingProtocol.BGP), hasPrefix(r3StaticPrefix))));
    assertThat(
        r3Routes, hasItem(allOf(hasProtocol(RoutingProtocol.BGP), hasPrefix(r1StaticPrefix))));
  }

  @Test
  public void testIbgpDynamicLocalIps() throws IOException {
    // See testEbgpDynamicLocalIps for setup. Only difference is peers are now iBGP.
    String testrigName = "ibgp-dynamic-local-ips";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();

    Set<AbstractRoute> r1Routes = ribs.get("r1").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(DEFAULT_VRF_NAME).getRoutes();

    // r1 and r3's respective static routes were redistributed into BGP and reached each other
    Prefix r1StaticPrefix = Prefix.parse("21.21.21.21/32");
    Prefix r3StaticPrefix = Prefix.parse("23.23.23.23/32");
    assertThat(
        r1Routes, hasItem(allOf(hasProtocol(RoutingProtocol.IBGP), hasPrefix(r3StaticPrefix))));
    assertThat(
        r3Routes, hasItem(allOf(hasProtocol(RoutingProtocol.IBGP), hasPrefix(r1StaticPrefix))));
  }

  @Test
  public void testIbgpRejectOwnAs() throws IOException {
    String testrigName = "ibgp-reject-own-as";
    List<String> configurationNames = ImmutableList.of("r1", "r2a", "r2b");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult dp = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp._dataPlane.getRibs();
    Set<AbstractRoute> r2aRoutes = ribs.get("r2a").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r2bRoutes = ribs.get("r2b").get(DEFAULT_VRF_NAME).getRoutes();
    Set<Prefix> r2aPrefixes =
        r2aRoutes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Set<Prefix> r2bPrefixes =
        r2bRoutes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = Prefix.parse("1.0.0.1/32");
    Prefix r1Loopback1Prefix = Prefix.parse("1.0.0.2/32");
    assertTrue(r2aPrefixes.contains(r1Loopback0Prefix));
    assertTrue(r2aPrefixes.contains(r1Loopback1Prefix));
    /*
     * 1.0.0.2/32 should be accepted r2b as a normal iBGP route forwarded from r1.
     */
    assertTrue(r2bPrefixes.contains(r1Loopback1Prefix));
    /*
     * 1.0.0.1/32 should be rejected by r2b since it already contains AS#2 in its AS-path due to
     * r2a prepending 2 in the matching route-map clause.
     */
    assertFalse(r2bPrefixes.contains(r1Loopback0Prefix));
  }

  @Test
  public void testIbgpRejectSameNeighborID() throws IOException {
    String testrigName = "ibgp-reject-routerid-match";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3", "r4");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult dp = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp._dataPlane.getRibs();

    Set<AbstractRoute> r2Routes = ribs.get("r2").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(DEFAULT_VRF_NAME).getRoutes();
    Set<Prefix> r2Prefixes =
        r2Routes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes =
        r3Routes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    // 9.9.9.9/32 is the prefix we test with
    Prefix r1AdvertisedPrefix = Prefix.parse("9.9.9.9/32");

    // Ensure that the prefix is accepted by r2, because router ids are different
    assertThat(r1AdvertisedPrefix, in(r2Prefixes));
    // Ensure that the prefix is rejected by r3, because router ids are the same
    assertThat(r1AdvertisedPrefix, not(in(r3Prefixes)));
  }

  @Test
  public void testStaticInterfaceRoutesWithoutEdge() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Interface i =
        nf.interfaceBuilder()
            .setOwner(c)
            .setVrf(vrf)
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .build();
    StaticRoute srBoth =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("10.0.1.0/24"))
            .setNextHopInterface(i.getName())
            .setNextHopIp(Ip.parse("10.0.0.2"))
            .setAdministrativeCost(1)
            .build();
    vrf.getStaticRoutes().add(srBoth);
    StaticRoute srJustInterface =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("10.0.2.0/24"))
            .setNextHopInterface(i.getName())
            .setAdministrativeCost(1)
            .build();
    vrf.getStaticRoutes().add(srJustInterface);
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            // TODO: parametrize settings with different schedules
            new IncrementalDataPlaneSettings());
    Topology topology = new Topology(Collections.emptySortedSet());
    ComputeDataPlaneResult dp =
        engine.computeDataPlane(
            configs,
            TopologyContext.builder().setLayer3Topology(topology).build(),
            Collections.emptySet());

    // generating fibs should not crash
    dp._dataPlane.getFibs();
  }

  @Test
  public void testStaticNextVrfRoute() {
    String hostname = "n1";
    String nextVrf = "nextVrf";
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf vrf = vb.setName(DEFAULT_VRF_NAME).build();
    vb.setName(nextVrf).build();
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(nextVrf))
            .setAdministrativeCost(1)
            .build();
    vrf.getStaticRoutes().add(sr);
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    ComputeDataPlaneResult dp =
        engine.computeDataPlane(configs, TopologyContext.builder().build(), Collections.emptySet());

    // generating fibs should not crash
    assertThat(
        dp._dataPlane.getRibs().get(hostname).get(DEFAULT_VRF_NAME).getRoutes(), contains(sr));
  }

  @Test
  public void testBgpNeighborReachability() throws IOException {
    // Only connect one neighbor (n2) to core router
    SortedMap<String, Configuration> configs = generateNetworkWithDuplicates();

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        result._topologies.getBgpTopology().getGraph();

    // N2 has proper neighbor relationship
    Set<Entry<Ip, BgpActivePeerConfig>> n2Neighbors =
        configs
            .get("n2")
            .getVrfs()
            .get(DEFAULT_VRF_NAME)
            .getBgpProcess()
            .getActiveNeighbors()
            .entrySet();
    Entry<Ip, BgpActivePeerConfig> e = n2Neighbors.iterator().next();
    assertThat(n2Neighbors, hasSize(1));
    assertThat(
        bgpTopology.degree(
            new BgpPeerConfigId("n2", DEFAULT_VRF_NAME, e.getKey().toPrefix(), false)),
        equalTo(2));

    // N1 does not have a full session established, because it's not reachable
    Collection<BgpActivePeerConfig> n1Neighbors =
        configs
            .get("n1")
            .getVrfs()
            .get(DEFAULT_VRF_NAME)
            .getBgpProcess()
            .getActiveNeighbors()
            .values();
    e = n2Neighbors.iterator().next();
    assertThat(n1Neighbors, hasSize(1));
    assertThat(
        bgpTopology.degree(
            new BgpPeerConfigId("n1", DEFAULT_VRF_NAME, e.getKey().toPrefix(), false)),
        equalTo(0));
  }

  @Test
  public void testGeneratedRoutesInMainRib() throws IOException {
    Configuration n1 =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();
    Vrf vrf = _vb.setOwner(n1).build();

    // Create generated route
    Prefix genRoutePrefix = Prefix.parse("1.1.1.1/32");
    Builder grb = GeneratedRoute.builder();
    GeneratedRoute route = grb.setNetwork(genRoutePrefix).setDiscard(true).build();
    vrf.setGeneratedRoutes(ImmutableSortedSet.of(route));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(n1.getHostname(), n1), _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    DataPlane dp = dataPlanePlugin.computeDataPlane(batfish.getSnapshot())._dataPlane;

    assertThat(
        dp.getRibs().get(n1.getHostname()).get(vrf.getName()).getRoutes(),
        hasItem(hasPrefix(genRoutePrefix)));
  }

  @Test
  public void testEbgpSinglehopSuccess() throws IOException {
    SortedMap<String, Configuration> configs = generateNetworkWithThreeHops(false, false);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = result._dataPlane;

    BgpPeerConfigId initiator =
        new BgpPeerConfigId("node1", "~Vrf_0~", Prefix.parse("1.0.0.0/32"), false);
    BgpPeerConfigId listener =
        new BgpPeerConfigId("node2", "~Vrf_1~", Prefix.parse("1.0.0.1/32"), false);

    Ip initiatorLocalIp = Ip.parse("1.0.0.0");
    BgpActivePeerConfig source =
        BgpActivePeerConfig.builder()
            .setLocalIp(initiatorLocalIp)
            .setPeerAddress(Ip.parse("1.0.0.1"))
            .setEbgpMultihop(false)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // the neighbor should be reachable because it is only one hop away from the initiator
    List<BgpSessionInitiationResult> initiationResults =
        BgpTopologyUtils.initiateBgpSessions(
            initiator,
            listener,
            source,
            ImmutableSet.of(initiatorLocalIp),
            new TracerouteEngineImpl(dp, result._topologies.getLayer3Topology(), configs));
    BgpSessionInitiationResult bgpSessionInitiationResult =
        Iterables.getOnlyElement(initiationResults);
    assertTrue(bgpSessionInitiationResult.isSuccessful());
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getForwardTraces()),
        hasHops(contains(hasNodeName("node1"), hasNodeName("node2"))));
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getReverseTraces()),
        hasHops(contains(hasNodeName("node2"), hasNodeName("node1"))));
  }

  @Test
  public void testEbgpSinglehopFailure() throws IOException {
    SortedMap<String, Configuration> configs = generateNetworkWithThreeHops(false, false);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = result._dataPlane;

    BgpPeerConfigId initiator =
        new BgpPeerConfigId("node1", "~Vrf_0~", Prefix.parse("1.0.0.0/32"), false);
    BgpPeerConfigId listener =
        new BgpPeerConfigId("node3", "~Vrf_2~", Prefix.parse("1.0.0.3/32"), false);

    Ip initiatorLocalIp = Ip.parse("1.0.0.0");
    BgpActivePeerConfig source =
        BgpActivePeerConfig.builder()
            .setLocalIp(initiatorLocalIp)
            .setPeerAddress(Ip.parse("1.0.0.3"))
            .setEbgpMultihop(false)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // the neighbor should be not be reachable because it is two hops away from the initiator
    List<BgpSessionInitiationResult> initiationResults =
        BgpTopologyUtils.initiateBgpSessions(
            initiator,
            listener,
            source,
            ImmutableSet.of(initiatorLocalIp),
            new TracerouteEngineImpl(dp, result._topologies.getLayer3Topology(), configs));
    BgpSessionInitiationResult bgpSessionInitiationResult =
        Iterables.getOnlyElement(initiationResults);
    assertFalse(bgpSessionInitiationResult.isSuccessful());
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getForwardTraces()),
        allOf(
            hasDisposition(FlowDisposition.ACCEPTED),
            hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3")))));
    assertTrue(bgpSessionInitiationResult.getReverseTraces().isEmpty());
  }

  @Test
  public void testEbgpMultihopSuccess() throws IOException {
    SortedMap<String, Configuration> configs = generateNetworkWithThreeHops(false, false);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = result._dataPlane;

    BgpPeerConfigId initiator =
        new BgpPeerConfigId("node1", "~Vrf_0~", Prefix.parse("1.0.0.0/32"), false);
    BgpPeerConfigId listener =
        new BgpPeerConfigId("node3", "~Vrf_2~", Prefix.parse("1.0.0.3/32"), false);

    Ip initiatorLocalIp = Ip.parse("1.0.0.0");
    BgpActivePeerConfig source =
        BgpActivePeerConfig.builder()
            .setLocalIp(initiatorLocalIp)
            .setPeerAddress(Ip.parse("1.0.0.3"))
            .setEbgpMultihop(true)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // the neighbor should be reachable because multi-hops are allowed
    List<BgpSessionInitiationResult> initiationResults =
        BgpTopologyUtils.initiateBgpSessions(
            initiator,
            listener,
            source,
            ImmutableSet.of(initiatorLocalIp),
            new TracerouteEngineImpl(dp, result._topologies.getLayer3Topology(), configs));
    BgpSessionInitiationResult bgpSessionInitiationResult =
        Iterables.getOnlyElement(initiationResults);
    assertTrue(bgpSessionInitiationResult.isSuccessful());
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getForwardTraces()),
        hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3"))));
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getReverseTraces()),
        hasHops(contains(hasNodeName("node3"), hasNodeName("node2"), hasNodeName("node1"))));
  }

  @Test
  public void testEbgpMultihopFailureWithAcl() throws IOException {
    // use a network with a deny all ACL on node 3
    SortedMap<String, Configuration> configs = generateNetworkWithThreeHops(true, false);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = result._dataPlane;

    BgpPeerConfigId initiator =
        new BgpPeerConfigId("node1", "~Vrf_0~", Prefix.parse("1.0.0.0/32"), false);
    BgpPeerConfigId listener =
        new BgpPeerConfigId("node3", "~Vrf_2~", Prefix.parse("1.0.0.3/32"), false);

    Ip initiatorLocalIp = Ip.parse("1.0.0.0");
    BgpActivePeerConfig source =
        BgpActivePeerConfig.builder()
            .setLocalIp(initiatorLocalIp)
            .setPeerAddress(Ip.parse("1.0.0.3"))
            .setEbgpMultihop(true)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // the neighbor should not be reachable even though multihops are allowed as traceroute would be
    // denied in on node 3
    List<BgpSessionInitiationResult> initiationResults =
        BgpTopologyUtils.initiateBgpSessions(
            initiator,
            listener,
            source,
            ImmutableSet.of(initiatorLocalIp),
            new TracerouteEngineImpl(dp, result._topologies.getLayer3Topology(), configs));
    BgpSessionInitiationResult bgpSessionInitiationResult =
        Iterables.getOnlyElement(initiationResults);
    assertFalse(bgpSessionInitiationResult.isSuccessful());
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getForwardTraces()),
        allOf(
            hasDisposition(FlowDisposition.DENIED_IN),
            hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3")))));
    assertTrue(bgpSessionInitiationResult.getReverseTraces().isEmpty());
  }

  @Test
  public void testEbgpWithAclPermitEstablished() throws IOException {
    // use a network with an allow established connection ACL on node1
    SortedMap<String, Configuration> configs = generateNetworkWithThreeHops(false, true);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult result = dataPlanePlugin.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = result._dataPlane;

    BgpPeerConfigId initiator =
        new BgpPeerConfigId("node1", "~Vrf_0~", Prefix.parse("1.0.0.0/32"), false);
    BgpPeerConfigId listener =
        new BgpPeerConfigId("node3", "~Vrf_2~", Prefix.parse("1.0.0.3/32"), false);

    Ip initiatorLocalIp = Ip.parse("1.0.0.0");
    BgpActivePeerConfig source =
        BgpActivePeerConfig.builder()
            .setLocalIp(initiatorLocalIp)
            .setPeerAddress(Ip.parse("1.0.0.3"))
            .setEbgpMultihop(true)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    // neighbor should be reachable because ACL allows established connection back into node1 and
    // allows everything out
    List<BgpSessionInitiationResult> initiationResults =
        BgpTopologyUtils.initiateBgpSessions(
            initiator,
            listener,
            source,
            ImmutableSet.of(initiatorLocalIp),
            new TracerouteEngineImpl(dp, result._topologies.getLayer3Topology(), configs));
    BgpSessionInitiationResult bgpSessionInitiationResult =
        Iterables.getOnlyElement(initiationResults);
    assertTrue(bgpSessionInitiationResult.isSuccessful());
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getForwardTraces()),
        hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3"))));
    assertThat(
        Iterables.getOnlyElement(bgpSessionInitiationResult.getReverseTraces()),
        hasHops(contains(hasNodeName("node3"), hasNodeName("node2"), hasNodeName("node1"))));
  }

  /**
   * Generates configurations for a three node network with connectivity as shown in the diagram
   * below. Also adds static routes from node 1 to node 3 and back from node 3 to node 1
   *
   * @param denyIntoHop3 If true, add an incoming ACL on node3 that blocks all traffic
   * @param allowOnlyEstablishedIntoHop1 If true, add an ACL on node1 which permits only established
   *     TCP connections back into node1
   * @return {@link SortedMap} of generated configuration names and corresponding {@link
   *     Configuration}s
   */

  /* +-----------+                       +-------------+                   +--------------+
     |           |1.0.0.0/31             |             |                   |              |
     |           +-----------------------+             |                   |    node3     |
     |   node1   |            1.0.0.1/31 |   node2     |1.0.0.2/31         |              |
     |           |                       |             +-------------------+              |
     |           |                       |             |         1.0.0.3/31|              |
     +-----------+                       +-------------+                   +--------------+

  */
  private static SortedMap<String, Configuration> generateNetworkWithThreeHops(
      boolean denyIntoHop3, boolean allowOnlyEstablishedIntoHop1) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    // first node
    Configuration c1 = cb.setHostname("node1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress c1Addr1 = ConcreteInterfaceAddress.parse("1.0.0.0/31");
    Interface i11 = nf.interfaceBuilder().setOwner(c1).setVrf(v1).setAddress(c1Addr1).build();

    // second node
    Configuration c2 = cb.setHostname("node2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    ConcreteInterfaceAddress c2Addr1 = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    nf.interfaceBuilder().setOwner(c2).setVrf(v2).setAddress(c2Addr1).build();
    ConcreteInterfaceAddress c2Addr2 = ConcreteInterfaceAddress.parse("1.0.0.2/31");
    nf.interfaceBuilder().setOwner(c2).setVrf(v2).setAddress(c2Addr2).build();

    // third node
    Configuration c3 = cb.setHostname("node3").build();
    Vrf v3 = nf.vrfBuilder().setOwner(c3).build();
    ConcreteInterfaceAddress c3Addr1 = ConcreteInterfaceAddress.parse("1.0.0.3/31");
    Interface i31 = nf.interfaceBuilder().setOwner(c3).setVrf(v3).setAddress(c3Addr1).build();

    // static routes on node1
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.3/32"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i11.getName())
                .setNextHopIp(c2Addr1.getIp())
                .build()));

    // static routes on node 3 to get back to node1
    v3.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/32"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i31.getName())
                .setNextHopIp(c2Addr2.getIp())
                .build()));

    if (denyIntoHop3) {
      // stop the flow from entering Node3
      i31.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c3)
              .setLines(
                  ImmutableList.of(
                      ExprAclLine.rejecting(AclLineMatchExprs.matchSrc(UniverseIpSpace.INSTANCE))))
              .build());
    }
    if (allowOnlyEstablishedIntoHop1) {
      i11.setOutgoingFilter(
          nf.aclBuilder().setOwner(c1).setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL)).build());
      i11.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c1)
              .setLines(
                  ImmutableList.of(
                      ExprAclLine.acceptingHeaderSpace(
                          HeaderSpace.builder()
                              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
                              .setTcpFlags(ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                              .build()),
                      REJECT_ALL))
              .build());
    }

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);
  }

  /**
   * Check that ibdp topology fixed-point computation is performed correctly. In particular, ensure
   * that iBGP adjacency are established between loopbacks over IS-IS as the IGP.
   */
  @Test
  public void testBgpOverIsis() throws IOException {
    /*
    *
    * Network setup: an IBGP loopback peering with ISIS as the IGP.
    *
                +-----+ 1.1.1.2/31           1.1.1.3/31+-----+
     1.1.1.1/32 |  n1 +--------------------------------+  n2 |2.2.2.2/32
                +-----+                                +-----+
    *
    */

    Ip lo1Ip = Ip.parse("1.1.1.1");
    Ip lo2Ip = Ip.parse("2.2.2.2");
    IsoAddress isoAddress1 = new IsoAddress("49.0001.0100.0100.1001.00");
    IsoAddress isoAddress2 = new IsoAddress("49.0001.0100.0200.2002.00");
    IsisInterfaceSettings isisInterfaceSettings =
        IsisInterfaceSettings.builder()
            .setPointToPoint(true)
            .setLevel2(
                IsisInterfaceLevelSettings.builder()
                    .setCost(10L)
                    .setMode(IsisInterfaceMode.ACTIVE)
                    .build())
            .build();

    // Node 1
    Configuration c1 =
        _cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).setHostname("n1").build();
    Vrf vrf1 = _vb.setName(DEFAULT_VRF_NAME).setOwner(c1).build();
    // Interfaces: loopback and connecting
    _ib.setOwner(c1)
        .setVrf(vrf1)
        .setName("Loopback0")
        .setType(InterfaceType.LOOPBACK)
        .setAddress(ConcreteInterfaceAddress.create(lo1Ip, Prefix.MAX_PREFIX_LENGTH))
        .setIsis(isisInterfaceSettings)
        .build();

    _ib.setOwner(c1)
        .setName("Ethernet0")
        .setType(InterfaceType.PHYSICAL)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/31"))
        .setIsis(isisInterfaceSettings)
        .build();
    // ISIS process
    IsisProcess.builder()
        .setNetAddress(isoAddress1)
        .setLevel1(null)
        .setLevel2(IsisLevelSettings.builder().build())
        .setVrf(vrf1)
        .build();
    // Bgp process and neighbor:
    BgpProcess bgpp1 = testBgpProcess(Ip.parse("1.1.1.2"));
    vrf1.setBgpProcess(bgpp1);
    _nb.setPeerAddress(lo2Ip)
        .setLocalAs(1L)
        .setLocalIp(lo1Ip)
        .setRemoteAs(1L)
        .setBgpProcess(bgpp1)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(_epb.setOwner(c1).build().getName())
                .build())
        .build();

    // Node 2
    Configuration c2 =
        _cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).setHostname("n2").build();
    Vrf vrf2 = _vb.setName(DEFAULT_VRF_NAME).setOwner(c2).build();
    // Interfaces: loopback and connecting
    _ib.setOwner(c2)
        .setVrf(vrf2)
        .setName("Loopback0")
        .setType(InterfaceType.LOOPBACK)
        .setAddress(ConcreteInterfaceAddress.create(lo2Ip, Prefix.MAX_PREFIX_LENGTH))
        .setIsis(isisInterfaceSettings)
        .build();
    _ib.setOwner(c2)
        .setName("Ethernet0")
        .setType(InterfaceType.PHYSICAL)
        .setVrf(vrf2)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.3/31"))
        .setIsis(isisInterfaceSettings)
        .build();
    // ISIS process
    IsisProcess.builder()
        .setNetAddress(isoAddress2)
        .setLevel1(null)
        .setLevel2(IsisLevelSettings.builder().build())
        .setVrf(vrf2)
        .build();
    BgpProcess bgpp2 = testBgpProcess(Ip.parse("1.1.1.3"));
    vrf2.setBgpProcess(bgpp2);
    // Bgp neighbor:
    _nb.setPeerAddress(lo1Ip)
        .setLocalAs(1L)
        .setLocalIp(lo2Ip)
        .setRemoteAs(1L)
        .setBgpProcess(bgpp2)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(_epb.setOwner(c2).build().getName())
                .build())
        .build();

    ImmutableSortedMap<String, Configuration> configs = ImmutableSortedMap.of("n1", c1, "n2", c2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    assertThat(
        batfish
            .getTopologyProvider()
            .getBgpTopology(batfish.getSnapshot())
            .getGraph()
            .edges()
            .size(),
        equalTo(2));
    BgpPeerConfigId bgpConfig1 =
        new BgpPeerConfigId("n1", DEFAULT_VRF_NAME, lo2Ip.toPrefix(), false);
    BgpPeerConfigId bgpConfig2 =
        new BgpPeerConfigId("n2", DEFAULT_VRF_NAME, lo1Ip.toPrefix(), false);
    assertThat(
        batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot()).getGraph().edges(),
        equalTo(
            ImmutableSet.of(
                EndpointPair.ordered(bgpConfig1, bgpConfig2),
                EndpointPair.ordered(bgpConfig2, bgpConfig1))));
  }

  @Test
  public void testGetForwardingAnalysisDeserialized() throws IOException {
    String hostname = "n1";
    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_IOS);
    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(hostname, c), _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane deserializedDataPlane = SerializationUtils.clone(batfish.loadDataPlane(snapshot));

    assertNotNull(deserializedDataPlane.getForwardingAnalysis());
  }
}
