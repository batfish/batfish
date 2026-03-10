package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.isNonRouting;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.applyDistributeList;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.computeDefaultInterAreaRouteToInject;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.filterInterAreaRoutesToPropagateAtABR;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.getIfaceAddressesForIntraAreaRoutes;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.transformInterAreaRoutesOnExportNonABR;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.transformIntraAreaRoutesOnExport;
import static org.batfish.dataplane.rib.RouteAdvertisement.Reason.ADD;
import static org.batfish.matchers.RouteAdvertisementMatchers.hasReason;
import static org.batfish.matchers.RouteAdvertisementMatchers.hasRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.OspfRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNeighborConfig;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfProcess.Builder;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link OspfRoutingProcess} */
public class OspfRoutingProcessTest {
  private static final ConcreteInterfaceAddress INACTIVE_ADDR =
      ConcreteInterfaceAddress.parse("1.1.1.1/24");
  private static final ConcreteInterfaceAddress ACTIVE_ADDR_1 =
      ConcreteInterfaceAddress.parse("2.2.2.2/24");
  private static final ConcreteInterfaceAddress ACTIVE_ADDR_1_NEIGHBOR =
      ConcreteInterfaceAddress.parse("2.2.2.3/24");
  private static final ConcreteInterfaceAddress ACTIVE_ADDR_1_NEIGHBOR2 =
      ConcreteInterfaceAddress.parse("2.2.2.4/24");
  private static final ConcreteInterfaceAddress ACTIVE_ADDR_2 =
      ConcreteInterfaceAddress.parse("3.3.3.3/24");
  private static final ConcreteInterfaceAddress ACTIVE_ADDR_3_SUMMARIZED =
      ConcreteInterfaceAddress.parse("10.10.10.10/24");
  private static final ConcreteInterfaceAddress PASSIVE_ADDR =
      ConcreteInterfaceAddress.parse("4.4.4.4/24");
  private static final ConcreteInterfaceAddress OSPF_DISABLED_ADDR =
      ConcreteInterfaceAddress.parse("5.5.5.5/24");
  private static final String HOSTNAME = "r1";
  private static final String VRF_NAME = "vrf";
  private static OspfArea AREA0_CONFIG;
  private final OspfTopology _emptyOspfTopology =
      new OspfTopology(ValueGraphBuilder.directed().build());
  private static final String INACTIVE_IFACE_NAME = "inactive";
  private static final String PASSIVE_IFACE_NAME = "passive";
  private static final String ACTIVE_IFACE_NAME = "active";
  private static final String OSPF_DISABLED_IFACE_NAME = "ospfDisabled";
  private OspfRoutingProcess _routingProcess;
  private Configuration _c;
  // Non-default, distinctive costs
  private static final Map<RoutingProtocol, Integer> ADMIN_COSTS =
      ImmutableMap.of(
          RoutingProtocol.OSPF,
          100,
          RoutingProtocol.OSPF_IS,
          150,
          RoutingProtocol.OSPF_IA,
          200,
          RoutingProtocol.OSPF_E1,
          220,
          RoutingProtocol.OSPF_E2,
          230);

  @Before
  public void setUp() {
    NetworkFactory nf = new NetworkFactory();
    _c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(HOSTNAME)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(VRF_NAME).setOwner(_c).build();
    OspfInterfaceSettings.Builder ospf =
        OspfInterfaceSettings.defaultSettingsBuilder().setProcess("1").setEnabled(true);
    Interface.Builder ib =
        nf.interfaceBuilder()
            .setVrf(vrf)
            .setOwner(_c)
            .setOspfSettings(ospf.setNetworkType(OspfNetworkType.POINT_TO_POINT).build())
            .setType(InterfaceType.PHYSICAL)
            .setBandwidth(1e8);
    Interface inactiveIface =
        ib.setAdminUp(false)
            .setName(INACTIVE_IFACE_NAME)
            .setAddresses(INACTIVE_ADDR)
            .setOspfSettings(ospf.setCost(10).build())
            .build();
    ib.setAdminUp(true);
    Interface activeIface =
        ib.setName(ACTIVE_IFACE_NAME)
            .setAddresses(ACTIVE_ADDR_1, ACTIVE_ADDR_2, ACTIVE_ADDR_3_SUMMARIZED)
            .setOspfSettings(ospf.setNetworkType(OspfNetworkType.POINT_TO_POINT).build())
            .build();
    Interface passiveIface =
        ib.setName(PASSIVE_IFACE_NAME)
            .setAddresses(PASSIVE_ADDR)
            .setOspfSettings(
                ospf.setNetworkType(OspfNetworkType.BROADCAST).setPassive(true).build())
            .build();
    Interface ospfDisabled =
        ib.setName(OSPF_DISABLED_IFACE_NAME)
            .setAddresses(OSPF_DISABLED_ADDR)
            .setOspfSettings(
                ospf.setPassive(false)
                    .setNetworkType(OspfNetworkType.BROADCAST)
                    .setEnabled(false)
                    .build())
            .build();
    RouteFilterList summarize10_8 =
        new RouteFilterList(
            "summarize 10.0.0.0/8",
            ImmutableList.of(
                new RouteFilterLine(
                    LineAction.DENY, PrefixRange.moreSpecificThan(Prefix.parse("10.0.0.0/8"))),
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.ALL)));
    _c.getRouteFilterLists().put(summarize10_8.getName(), summarize10_8);
    AREA0_CONFIG =
        OspfArea.builder()
            .setNumber(0)
            .setInterfaces(
                ImmutableSet.of(
                    inactiveIface.getName(),
                    activeIface.getName(),
                    passiveIface.getName(),
                    ospfDisabled.getName(),
                    "NonExistent"))
            .setSummaryFilter(summarize10_8.getName())
            .build();

    OspfArea area1Config = OspfArea.builder().setNumber(1).build();

    OspfNeighborConfigId id =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);

    OspfProcess ospfProcess =
        nf.ospfProcessBuilder()
            .setProcessId("1")
            .setRouterId(ACTIVE_ADDR_1.getIp())
            .setReferenceBandwidth(10e9)
            .setVrf(vrf)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG, 1L, area1Config))
            .setAdminCosts(ADMIN_COSTS)
            .setNeighborConfigs(
                ImmutableMap.of(
                    id,
                    OspfNeighborConfig.builder()
                        .setHostname(HOSTNAME)
                        .setVrfName(VRF_NAME)
                        .setInterfaceName(ACTIVE_IFACE_NAME)
                        .setIp(ACTIVE_ADDR_1.getIp())
                        .setArea(0L)
                        .build()))
            .build();
    _routingProcess = new OspfRoutingProcess(ospfProcess, VRF_NAME, _c, _emptyOspfTopology);
  }

  private static OspfTopology nonEmptyOspfTopology() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().build();
    OspfNeighborConfigId c1 =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);
    OspfNeighborConfigId c2 =
        new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface", ACTIVE_ADDR_1_NEIGHBOR);
    // Add edges both directions
    graph.putEdgeValue(
        c1,
        c2,
        new OspfSessionProperties(
            0L, new IpLink(ACTIVE_ADDR_1.getIp(), ACTIVE_ADDR_1_NEIGHBOR.getIp())));
    graph.putEdgeValue(
        c2,
        c1,
        new OspfSessionProperties(
            0L, new IpLink(ACTIVE_ADDR_1_NEIGHBOR.getIp(), ACTIVE_ADDR_1.getIp())));
    return new OspfTopology(graph);
  }

  @Test
  public void testApplyDistributeList() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("conf")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(c)
            .setVrf(vrf)
            .setAddresses(ACTIVE_ADDR_1)
            .setOspfSettings(
                OspfInterfaceSettings.defaultSettingsBuilder()
                    .setInboundDistributeListPolicy("policy")
                    .build())
            .build();

    RouteFilterList prefixList =
        new RouteFilterList(
            "prefix_list",
            ImmutableList.of(
                new RouteFilterLine(
                    LineAction.PERMIT, PrefixRange.fromPrefix(ACTIVE_ADDR_2.getPrefix())),
                new RouteFilterLine(
                    LineAction.DENY, PrefixRange.fromPrefix(ACTIVE_ADDR_1.getPrefix()))));
    c.getRouteFilterLists().put(prefixList.getName(), prefixList);

    nf.routingPolicyBuilder()
        .setName("policy")
        .setOwner(c)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(), new NamedPrefixSet("prefix_list")),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();

    OspfIntraAreaRoute.Builder allowedRouteBuilder =
        OspfIntraAreaRoute.builder()
            .setNextHop(NextHopInterface.of("e0", Ip.parse("9.9.9.9")))
            .setNetwork(ACTIVE_ADDR_2.getPrefix())
            .setArea(1L);

    OspfIntraAreaRoute.Builder deniedRouteBuilder =
        OspfIntraAreaRoute.builder()
            .setNextHop(NextHopInterface.of("e0", Ip.parse("9.9.9.9")))
            .setNetwork(ACTIVE_ADDR_1.getPrefix())
            .setArea(1L);

    applyDistributeList(c, vrf.getName(), i1.getName(), allowedRouteBuilder);

    assertFalse(allowedRouteBuilder.getNonRouting());

    applyDistributeList(c, vrf.getName(), i1.getName(), deniedRouteBuilder);

    assertTrue(deniedRouteBuilder.getNonRouting());
  }

  /** Check that a new (but un-initialized) process is not dirty */
  @Test
  public void testNotDirtyOnCreation() {
    assertFalse(_routingProcess.isDirty());
  }

  @Test
  public void testDirtyPostInitialization() {
    _routingProcess.initialize(new Node(_c));
    assertTrue(_routingProcess.isDirty());
  }

  /**
   * Ensure that if no new incoming messages are supplied, one iteration is sufficient to return to
   * non-dirty state
   */
  @Test
  public void testNotDirtyAfterOneIteration() {
    _routingProcess.initialize(new Node(_c));
    // Empty map in this particular case just means no valid neighbors.
    _routingProcess.executeIteration(ImmutableMap.of());
    assertTrue("Still have updates to main RIB", _routingProcess.isDirty());
    _routingProcess.getUpdatesForMainRib();

    // Initialization delta should have been cleared
    assertFalse("All dirty state is cleared", _routingProcess.isDirty());
  }

  @Test
  public void testInitializeRoutesByArea() {
    // Must not crash on non-existent interface
    RibDelta<OspfIntraAreaRoute> delta = _routingProcess.initializeRoutesByArea(AREA0_CONFIG);
    // All routes were added
    assertTrue(delta.stream().allMatch(r -> r.getReason() == ADD));

    /*
      Requirements:
    - Must skip inactive interface
    - Must skip OSPF disabled interface
    - Must include OSPF passive interface
    - Must include all addresses from active interface, even if they would be summarized
    */
    assertThat(
        delta.getRoutesStream().collect(Collectors.toList()),
        containsInAnyOrder(
            hasPrefix(PASSIVE_ADDR.getPrefix()),
            hasPrefix(ACTIVE_ADDR_1.getPrefix()),
            hasPrefix(ACTIVE_ADDR_2.getPrefix()),
            hasPrefix(ACTIVE_ADDR_3_SUMMARIZED.getPrefix())));
  }

  @Test
  public void testInitializeInterAreaRoutes() {
    OspfIntraAreaRoute notSummarized =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .setNonRouting(true)
            .build();
    OspfIntraAreaRoute summarized =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.parse("10.10.10.10/32"))
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .setNonRouting(true)
            .build();
    RibDelta<OspfIntraAreaRoute> delta =
        RibDelta.<OspfIntraAreaRoute>builder().add(notSummarized).add(summarized).build();

    // Only the notSummarized network should be added to the inter-area delta.
    assertThat(
        _routingProcess.initializeInterAreaRoutes(delta).getPrefixes().collect(Collectors.toList()),
        contains(notSummarized.getNetwork()));
  }

  @Test
  public void testGetIncrementalCost() {
    // Default behavior
    assertThat(_routingProcess.getIncrementalCost(ACTIVE_IFACE_NAME, false), equalTo(10L));

    // Overriden behavior for transit links
    final long overrideMetric = 8888L;
    Builder ospfProcess =
        OspfProcess.builder()
            .setProcessId("1")
            .setRouterId(Ip.ZERO)
            .setReferenceBandwidth(10e9)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG))
            .setMaxMetricTransitLinks(overrideMetric);

    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost(ACTIVE_IFACE_NAME, false),
        equalTo(overrideMetric));

    // Overriden behavior for stub links
    ospfProcess.setMaxMetricTransitLinks(null).setMaxMetricStubNetworks(overrideMetric);
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost(ACTIVE_IFACE_NAME, false),
        equalTo(10L));
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost(ACTIVE_IFACE_NAME, true),
        equalTo(overrideMetric));
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost(PASSIVE_IFACE_NAME, false),
        equalTo(overrideMetric));
  }

  @Test
  public void testComputeDefaultInterAreaRouteToInject() {
    OspfArea.Builder areaBuilder = OspfArea.builder().setNumber(1L);
    Ip nextHopIp = Ip.parse("1.1.1.1");
    assertFalse(computeDefaultInterAreaRouteToInject(areaBuilder.build(), nextHopIp).isPresent());
    // No origination type set, so no route generated
    assertFalse(
        computeDefaultInterAreaRouteToInject(
                areaBuilder
                    .setStubType(StubType.NSSA)
                    .setNssa(NssaSettings.builder().build())
                    .build(),
                nextHopIp)
            .isPresent());
    // NSSA configured for default route injection
    assertThat(
        computeDefaultInterAreaRouteToInject(
                areaBuilder
                    .setStubType(StubType.NSSA)
                    .setNssa(
                        NssaSettings.builder()
                            .setDefaultOriginateType(OspfDefaultOriginateType.INTER_AREA)
                            .build())
                    .build(),
                nextHopIp)
            .get(),
        allOf(
            hasRoute(allOf(hasPrefix(Prefix.ZERO), hasNextHopIp(equalTo(nextHopIp)))),
            hasReason(ADD)));
    // STUB area
    assertThat(
        computeDefaultInterAreaRouteToInject(
                areaBuilder
                    .setStubType(StubType.STUB)
                    .setStubSettings(StubSettings.builder().build())
                    .build(),
                nextHopIp)
            .get(),
        allOf(
            hasRoute(allOf(hasPrefix(Prefix.ZERO), hasNextHopIp(equalTo(nextHopIp)))),
            hasReason(ADD)));
  }

  @Test
  public void testComputeDefaultInterAreaRouteToInjectRepeatedCalls() {
    OspfArea area =
        OspfArea.builder()
            .setNumber(1L)
            .setStubType(StubType.STUB)
            .setStubSettings(StubSettings.builder().build())
            .build();
    Ip nextHopIp = ACTIVE_ADDR_1.getIp();
    OspfNeighborConfigId n1 =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface", ACTIVE_ADDR_1_NEIGHBOR);
    EdgeId edge = OspfTopology.makeEdge(n1, n2);

    // First call allows injection of default route
    assertThat(
        _routingProcess
            .computeDefaultInterAreaRouteToInject(edge, area, nextHopIp)
            .collect(ImmutableList.toImmutableList()),
        contains(
            allOf(
                hasRoute(allOf(hasPrefix(Prefix.ZERO), hasNextHopIp(equalTo(nextHopIp)))),
                hasReason(ADD))));

    // Any subsequent calls for the same edge -- no need to inject default route
    for (int i = 0; i < 10; i++) {
      assertThat(
          "No default route to inject",
          _routingProcess.computeDefaultInterAreaRouteToInject(edge, area, nextHopIp).count(),
          equalTo(0L));
    }

    // For a edge with a different tail node, we still get a default route
    OspfNeighborConfigId n3 =
        new OspfNeighborConfigId("r3", VRF_NAME, "1", "someIface", ACTIVE_ADDR_1_NEIGHBOR2);
    edge = OspfTopology.makeEdge(n3, n2);
    assertThat(
        _routingProcess
            .computeDefaultInterAreaRouteToInject(edge, area, nextHopIp)
            .collect(ImmutableList.toImmutableList()),
        contains(
            allOf(
                hasRoute(allOf(hasPrefix(Prefix.ZERO), hasNextHopIp(equalTo(nextHopIp)))),
                hasReason(ADD))));
  }

  @Test
  public void testUpdateTopology() {
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    OspfNeighborConfigId n1 =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface", ACTIVE_ADDR_1_NEIGHBOR);

    // Both of these should not crash because new message queues exist now
    _routingProcess.enqueueMessagesIntra(OspfTopology.makeEdge(n2, n1), Stream.of());
    _routingProcess.enqueueMessagesInter(OspfTopology.makeEdge(n2, n1), ImmutableSet.of());
  }

  @Test
  public void updateTopologyIsNonDestructive() {
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    OspfNeighborConfigId n1 =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface", ACTIVE_ADDR_1_NEIGHBOR);
    _routingProcess.enqueueMessagesIntra(
        OspfTopology.makeEdge(n2, n1),
        Stream.of(
            new RouteAdvertisement<>(
                OspfIntraAreaRoute.builder()
                    .setArea(0)
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("8.8.8.8"))
                    .setNonRouting(true)
                    .build())));
    // Re-update topology
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    // Ensure still in dirty state (didn't lose the queued advertisement)
    assertTrue(_routingProcess.isDirty());
  }

  @Test
  public void testTransformIntraAreaRouteOnImport() {
    final OspfIntraAreaRoute.Builder builder =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.parse("1.1.1.1/29"))
            .setMetric(1)
            .setNonRouting(true)
            .setNonForwarding(true)
            .setNextHopIp(Ip.parse("8.8.8.8"));

    OspfIntraAreaRoute.Builder transformedBuilder =
        _routingProcess.transformIntraAreaRouteOnImport(builder.build(), "e1", 10);

    // Update metric, admin, clear non-routing bit
    assertThat(
        transformedBuilder.build(),
        equalTo(
            builder
                .setMetric(11L)
                .setAdmin(100)
                .setNextHopInterface("e1")
                .setNonRouting(false)
                .setNonForwarding(false)
                .build()));
  }

  @Test
  public void testTransformInterAreaRouteOnImport() {
    final OspfInterAreaRoute.Builder builder =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNonRouting(true)
            .setNonForwarding(true)
            .setNextHopIp(Ip.parse("8.8.8.8"));

    Optional<OspfInterAreaRoute.Builder> transformedBuilder =
        _routingProcess.transformInterAreaRouteOnImport(builder.build(), "e1", 10);
    assertFalse("Default route rejected by ABR", transformedBuilder.isPresent());

    // Non-default route
    builder.setNetwork(Prefix.parse("1.1.1.1/29"));
    transformedBuilder = _routingProcess.transformInterAreaRouteOnImport(builder.build(), "e1", 10);
    // Update metric, admin, clear non-routing bit
    assertTrue(transformedBuilder.isPresent());
    assertThat(
        transformedBuilder.get().build(),
        equalTo(
            builder
                .setMetric(11L)
                .setAdmin(200)
                .setNextHopInterface("e1")
                .setNonRouting(false)
                .setNonForwarding(false)
                .build()));
  }

  @Test
  public void testFilterInterAreaRoutesToPropagateAtABR() {
    OspfInterAreaRoute route0 =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();
    OspfInterAreaRoute route1 =
        OspfInterAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setMetric(42)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    Ip neighborIp = Ip.parse("2.2.2.2");
    final RibDelta<OspfInterAreaRoute> delta =
        RibDelta.<OspfInterAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    List<RouteAdvertisement<OspfInterAreaRoute>> transformed =
        filterInterAreaRoutesToPropagateAtABR(delta, AREA0_CONFIG, neighborIp, nextHopIp, null)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route1.getMetric()),
                    isNonRouting(true)))));

    // Regular conversion but with custom metric
    transformed =
        filterInterAreaRoutesToPropagateAtABR(delta, AREA0_CONFIG, neighborIp, nextHopIp, 999L)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(999L),
                    isNonRouting(true)))));

    // Convert but filter because STUB and suppress type 3
    transformed =
        filterInterAreaRoutesToPropagateAtABR(
                delta,
                OspfArea.builder()
                    .setStub(StubSettings.builder().setSuppressType3(true).build())
                    .setNumber(2)
                    .build(),
                neighborIp,
                nextHopIp,
                null)
            .collect(Collectors.toList());
    assertThat(transformed, empty());

    // Convert but filter because NSSA and suppress type 3
    transformed =
        filterInterAreaRoutesToPropagateAtABR(
                delta,
                OspfArea.builder()
                    .setNssa(NssaSettings.builder().setSuppressType3(true).build())
                    .setNumber(2)
                    .build(),
                neighborIp,
                nextHopIp,
                null)
            .collect(Collectors.toList());
    assertThat(transformed, empty());
  }

  @Test
  public void testTransformInterAreaRoutesOnExportNonABR() {
    OspfInterAreaRoute route0 =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();
    OspfInterAreaRoute route1 =
        OspfInterAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfInterAreaRoute> delta =
        RibDelta.<OspfInterAreaRoute>builder().add(route0).add(route1).build();

    Ip neighborIp = Ip.parse("2.2.2.2");

    // Regular conversion
    Collection<RouteAdvertisement<OspfInterAreaRoute>> transformed =
        transformInterAreaRoutesOnExportNonABR(delta, AREA0_CONFIG, neighborIp, nextHopIp, null);
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route0.getMetric()),
                    isNonRouting(true)))));

    // Regular conversion but with custom metric
    transformed =
        transformInterAreaRoutesOnExportNonABR(delta, AREA0_CONFIG, neighborIp, nextHopIp, 999L);
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(999L),
                    isNonRouting(true)))));
  }

  @Test
  public void testTransformIntraAreaRoutesOnExport() {
    OspfIntraAreaRoute route0 =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();
    OspfIntraAreaRoute route1 =
        OspfIntraAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8")))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfIntraAreaRoute> delta =
        RibDelta.<OspfIntraAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    Collection<RouteAdvertisement<OspfIntraAreaRoute>> transformed =
        transformIntraAreaRoutesOnExport(delta, AREA0_CONFIG, nextHopIp)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route0.getMetric()),
                    isNonRouting(true)))));
  }

  @Test
  public void testTransformType1RouteOnImport() {
    final OspfExternalRoute.Builder builder =
        OspfExternalType1Route.builder()
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setLsaMetric(0L)
            .setCostToAdvertiser(0L)
            .setAdvertiser("someNode")
            .setArea(1L)
            .setAdmin(0)
            .setNonRouting(true)
            .setNextHopIp(Ip.parse("1.1.1.1"));
    final Ip nextHopIp = Ip.parse("9.9.9.9");

    assertThat(
        _routingProcess
            .transformType1RouteOnImport(
                (OspfExternalType1Route) builder.build(), "e1", nextHopIp, 10)
            .build(),
        equalTo(
            builder
                .setCostToAdvertiser(10)
                .setMetric(10)
                .setNextHop(NextHopInterface.of("e1", nextHopIp))
                .setAdmin(220)
                .setNonRouting(false)
                .build()));
  }

  @Test
  public void testTransformType2RouteOnImport() {
    final OspfExternalRoute.Builder builder =
        OspfExternalType2Route.builder()
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setLsaMetric(0L)
            .setCostToAdvertiser(0L)
            .setAdvertiser("someNode")
            .setArea(1L)
            .setAdmin(0)
            .setNonRouting(true)
            .setNextHopIp(Ip.parse("1.1.1.1"));

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    assertThat(
        _routingProcess
            .transformType2RouteOnImport(
                (OspfExternalType2Route) builder.build(), "e1", nextHopIp, 10)
            .build(),
        equalTo(
            builder
                .setCostToAdvertiser(10)
                .setNextHop(NextHopInterface.of("e1", nextHopIp))
                .setNonRouting(false)
                .setAdmin(230)
                .build()));
  }

  @Test
  public void testTransformType1RouteOnExportNoMetricOverride() {
    Ip nextHopIp = Ip.parse("1.1.1.1");
    final OspfExternalRoute base =
        OspfExternalType1Route.builder()
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setLsaMetric(0L)
            .setCostToAdvertiser(0L)
            .setAdvertiser("someNode")
            .setArea(1L)
            .setAdmin(0)
            .setNextHop(NextHopDiscard.instance())
            .build();
    final OspfArea ospfArea = OspfArea.builder().setNumber(33).build();

    // Going to area 0
    assertThat(
        _routingProcess.transformType1RoutesOnExport(
            Stream.of(new RouteAdvertisement<>((OspfExternalType1Route) base)),
            AREA0_CONFIG,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(0)
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));

    // Going from area 0 to some area
    assertThat(
        _routingProcess.transformType1RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType1Route) base.toBuilder().setArea(0).build())),
            ospfArea,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(ospfArea.getAreaNumber())
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));

    // Generated locally, going to some area
    assertThat(
        _routingProcess.transformType1RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType1Route) base.toBuilder().setArea(OspfRoute.NO_AREA).build())),
            ospfArea,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(ospfArea.getAreaNumber())
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));

    // In the same area
    assertThat(
        _routingProcess.transformType1RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType1Route)
                        base.toBuilder().setArea(ospfArea.getAreaNumber()).build())),
            ospfArea,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(ospfArea.getAreaNumber())
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));

    // Going across two non-zero areas: not allowed
    assertThat(
        _routingProcess.transformType1RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType1Route)
                        base.toBuilder().setArea(ospfArea.getAreaNumber()).build())),
            OspfArea.builder().setNumber(44).build(),
            nextHopIp),
        empty());
  }

  @Test
  public void testTransformType1RouteOnExportWithMetricOverride() {
    Ip nextHopIp = Ip.parse("1.1.1.1");
    // Overriden behavior for summary routes
    final long overrideMetric = 8888L;
    Builder ospfProcess =
        OspfProcess.builder()
            .setRouterId(Ip.ZERO)
            .setProcessId("1")
            .setReferenceBandwidth(10e9)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG))
            .setMaxMetricSummaryNetworks(overrideMetric);
    OspfRoutingProcess routingProcess =
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology);
    final OspfExternalRoute base =
        OspfExternalType1Route.builder()
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setLsaMetric(10L)
            .setCostToAdvertiser(0L)
            .setAdvertiser("someNode")
            .setArea(1L)
            .setAdmin(0)
            .setNextHop(NextHopDiscard.instance())
            .build();
    // Override for routes transiting across areas
    assertThat(
        routingProcess.transformType1RoutesOnExport(
            Stream.of(new RouteAdvertisement<>((OspfExternalType1Route) base.toBuilder().build())),
            AREA0_CONFIG,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                (OspfExternalType1Route)
                    base.toBuilder()
                        .setCostToAdvertiser(overrideMetric)
                        .setMetric(overrideMetric + 10) // + LsaMetric
                        .setArea(0)
                        .setNonRouting(true)
                        .setNextHop(NextHopIp.of(nextHopIp))
                        .build())));

    // No override for locally generated routes
    assertThat(
        routingProcess.transformType1RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType1Route) base.toBuilder().setArea(OspfRoute.NO_AREA).build())),
            AREA0_CONFIG,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                (OspfExternalType1Route)
                    base.toBuilder()
                        .setCostToAdvertiser(overrideMetric)
                        .setMetric(0) // not overridden
                        .setArea(0)
                        .setNonRouting(true)
                        .setNextHop(NextHopIp.of(nextHopIp))
                        .build())));
  }

  @Test
  public void testTransformType2RouteOnExport() {
    Ip nextHopIp = Ip.parse("1.1.1.1");
    final OspfExternalRoute base =
        OspfExternalType2Route.builder()
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setLsaMetric(0L)
            .setCostToAdvertiser(0L)
            .setAdvertiser("someNode")
            .setArea(1L)
            .setAdmin(0)
            .setNextHop(NextHopDiscard.instance())
            .build();
    assertThat(
        OspfRoutingProcess.transformType2RoutesOnExport(
            Stream.of(new RouteAdvertisement<>((OspfExternalType2Route) base.toBuilder().build())),
            AREA0_CONFIG,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(1)
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));
    assertThat(
        OspfRoutingProcess.transformType2RoutesOnExport(
            Stream.of(
                new RouteAdvertisement<>(
                    (OspfExternalType2Route) base.toBuilder().setArea(OspfRoute.NO_AREA).build())),
            AREA0_CONFIG,
            nextHopIp),
        contains(
            new RouteAdvertisement<>(
                base.toBuilder()
                    .setArea(0)
                    .setNonRouting(true)
                    .setNextHop(NextHopIp.of(nextHopIp))
                    .build())));
  }

  @Test
  public void testActivateGeneratedRoute() {
    RibDelta<AnnotatedRoute<AbstractRoute>> delta =
        RibDelta.<AnnotatedRoute<AbstractRoute>>builder()
            .add(
                new AnnotatedRoute<>(
                    StaticRoute.testBuilder()
                        .setAdministrativeCost(1)
                        .setNetwork(Prefix.parse("1.1.1.0/24"))
                        .setNextHopInterface("Null")
                        .build(),
                    VRF_NAME))
            .build();

    final GeneratedRoute.Builder generatedRoute = GeneratedRoute.builder().setNetwork(Prefix.ZERO);

    final String policyName = "GEN_POLICY";
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(policyName)
        .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
        .build();

    // Unconditional generation
    assertThat(
        _routingProcess.activateGeneratedRoute(delta, generatedRoute.build()),
        equalTo(generatedRoute.build()));
    // Conditional generation
    assertThat(
        _routingProcess.activateGeneratedRoute(
            delta, generatedRoute.setGenerationPolicy(policyName).build()),
        nullValue());
    // Conditional generation with missing policy
    assertThat(
        _routingProcess.activateGeneratedRoute(
            delta, generatedRoute.setGenerationPolicy("nonexistent").build()),
        nullValue());
  }

  @Test
  public void testConvertToExternalRoute() {
    StaticRoute.Builder sb =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.parse("2.2.2.2/32"))
            .setNextHopInterface("Null");
    StaticRoute route = sb.build();

    RoutingPolicy exportPolicy =
        RoutingPolicy.builder()
            .setOwner(_c)
            .setName("EXPORT_POLICY")
            .setStatements(
                ImmutableList.of(
                    new If(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(),
                            new ExplicitPrefixSet(
                                new PrefixSpace(
                                    PrefixRange.fromPrefix(Prefix.parse("2.2.2.2/32"))))),
                        ImmutableList.of(
                            new SetOspfMetricType(OspfMetricType.E1),
                            Statements.ExitAccept.toStaticStatement()),
                        ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
            .build();

    // Allowed to export
    assertThat(
        _routingProcess.convertToExternalRoute(route, exportPolicy).get(),
        equalTo(
            OspfExternalType1Route.builder()
                .setNetwork(route.getNetwork())
                .setNextHop(NextHopDiscard.instance())
                .setMetric(0)
                .setLsaMetric(0)
                .setCostToAdvertiser(0)
                .setArea(OspfRoute.NO_AREA)
                .setAdmin(220)
                .setAdvertiser(_c.getHostname())
                .setNonRouting(true)
                .build()));
    // Not allowed to export
    assertFalse(
        "Export policy does not permit export",
        _routingProcess
            .convertToExternalRoute(sb.setNetwork(Prefix.parse("1.1.1.1/32")).build(), exportPolicy)
            .isPresent());
  }

  @Test
  public void testFilterExternalRoutesOnExport() {
    RibDelta<OspfExternalRoute> routes =
        RibDelta.<OspfExternalRoute>builder()
            .add(
                OspfExternalType1Route.builder()
                    .setNetwork(Prefix.parse("1.1.1.1/32"))
                    .setNextHop(NextHopDiscard.instance())
                    .setLsaMetric(0L)
                    .setCostToAdvertiser(0L)
                    .setAdvertiser("someNode")
                    .setArea(OspfRoute.NO_AREA)
                    .build())
            .build();
    // No filtering to regular area
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(routes, AREA0_CONFIG, null)
            .collect(Collectors.toList()),
        equalTo(routes.stream().collect(Collectors.toList())));
    // Filtering to STUB
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(
                routes,
                OspfArea.builder().setNumber(33).setStub(StubSettings.builder().build()).build(),
                null)
            .collect(Collectors.toList()),
        empty());
    // Filtering to NSSA with type7 suppression
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(
                routes,
                OspfArea.builder()
                    .setNumber(33)
                    .setNssa(NssaSettings.builder().setSuppressType7(true).build())
                    .build(),
                null)
            .collect(Collectors.toList()),
        empty());
    // No filtering to NSSA without type7 suppression
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(
                routes,
                OspfArea.builder()
                    .setNumber(33)
                    .setNssa(NssaSettings.builder().setSuppressType7(false).build())
                    .build(),
                null)
            .collect(Collectors.toList()),
        equalTo(routes.stream().collect(Collectors.toList())));
  }

  @Test
  public void testFilterExternalRoutesOnExportWithInterfaceFilter() {
    RoutingPolicy rp_accept =
        RoutingPolicy.builder()
            .setName("~OSPF_TYPE_5_FILTER~")
            .setOwner(_c)
            .addStatement(Statements.ExitAccept.toStaticStatement())
            .build();
    RoutingPolicy rp_reject =
        RoutingPolicy.builder()
            .setName("~OSPF_TYPE_5_FILTER~")
            .setOwner(_c)
            .addStatement(Statements.ExitReject.toStaticStatement())
            .build();

    RibDelta<OspfExternalRoute> routes =
        RibDelta.<OspfExternalRoute>builder()
            .add(
                OspfExternalType1Route.builder()
                    .setNetwork(Prefix.parse("1.1.1.1/32"))
                    .setNextHop(NextHopDiscard.instance())
                    .setLsaMetric(0L)
                    .setCostToAdvertiser(0L)
                    .setAdvertiser("someNode")
                    .setArea(OspfRoute.NO_AREA)
                    .build())
            .build();
    // Accept is a no-op - the route does not get filtered.
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(routes, AREA0_CONFIG, rp_accept)
            .collect(Collectors.toList()),
        equalTo(routes.stream().collect(Collectors.toList())));

    // Reject means the route gets filtered.
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(routes, AREA0_CONFIG, rp_reject)
            .collect(Collectors.toList()),
        empty());

    // type7 (to NSSA) do not get filtered
    assertThat(
        _routingProcess
            .filterExternalRoutesOnExport(
                routes,
                OspfArea.builder().setNumber(33).setNssa(NssaSettings.builder().build()).build(),
                rp_reject)
            .collect(Collectors.toList()),
        equalTo(routes.stream().collect(Collectors.toList())));
  }

  // For p2mp interfaces, initial route is the interface IP instead of subnet
  @Test
  public void testInitializeRoutesByArea_p2mp() {
    NetworkFactory nf = new NetworkFactory();
    _c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(HOSTNAME)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(VRF_NAME).setOwner(_c).build();
    OspfInterfaceSettings.Builder ospf =
        OspfInterfaceSettings.defaultSettingsBuilder().setProcess("1").setEnabled(true);

    Interface activeIface =
        nf.interfaceBuilder()
            .setAdminUp(true)
            .setVrf(vrf)
            .setOwner(_c)
            .setType(InterfaceType.PHYSICAL)
            .setBandwidth(1e8)
            .setName(ACTIVE_IFACE_NAME)
            .setAddresses(ACTIVE_ADDR_1)
            .setOspfSettings(
                ospf.setCost(1).setNetworkType(OspfNetworkType.POINT_TO_MULTIPOINT).build())
            .build();

    AREA0_CONFIG =
        OspfArea.builder()
            .setNumber(0)
            .setInterfaces(ImmutableSet.of(activeIface.getName()))
            .build();

    OspfNeighborConfigId id =
        new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", ACTIVE_IFACE_NAME, ACTIVE_ADDR_1);

    OspfProcess ospfProcess =
        nf.ospfProcessBuilder()
            .setProcessId("1")
            .setRouterId(ACTIVE_ADDR_1.getIp())
            .setReferenceBandwidth(10e9)
            .setVrf(vrf)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG))
            .setAdminCosts(ADMIN_COSTS)
            .setNeighborConfigs(
                ImmutableMap.of(
                    id,
                    OspfNeighborConfig.builder()
                        .setHostname(HOSTNAME)
                        .setVrfName(VRF_NAME)
                        .setInterfaceName(ACTIVE_IFACE_NAME)
                        .setIp(ACTIVE_ADDR_1.getIp())
                        .setArea(0L)
                        .build()))
            .build();
    _routingProcess = new OspfRoutingProcess(ospfProcess, VRF_NAME, _c, _emptyOspfTopology);
    RibDelta<OspfIntraAreaRoute> routes = _routingProcess.initializeRoutesByArea(AREA0_CONFIG);
    assertThat(
        routes.stream().collect(ImmutableList.toImmutableList()),
        contains(
            new RouteAdvertisement<>(
                new OspfIntraAreaRoute(
                    Prefix.parse("2.2.2.2/32"),
                    NextHopIp.of(Ip.parse("2.2.2.2")),
                    100,
                    1,
                    0,
                    -1,
                    false,
                    true))));
  }

  @Test
  public void testGetIfaceAddressesForIARoutes() {
    ConcreteInterfaceAddress primary = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    ConcreteInterfaceAddress secondary = ConcreteInterfaceAddress.parse("2.2.2.1/24");
    Interface.Builder ifaceBuilder =
        TestInterface.builder().setName("iface").setAddresses(primary, secondary);

    // Default case (not loopback or P2MP): Should return stream of all interface addresses
    assertThat(
        getIfaceAddressesForIntraAreaRoutes(ifaceBuilder.build())
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(primary, secondary));

    // P2MP: Should return all interface addresses converted to /32s
    ConcreteInterfaceAddress primarySlash32 = ConcreteInterfaceAddress.create(primary.getIp(), 32);
    ConcreteInterfaceAddress secondarySlash32 =
        ConcreteInterfaceAddress.create(secondary.getIp(), 32);
    OspfInterfaceSettings.Builder ospfSettings = OspfInterfaceSettings.builder().setPassive(false);
    OspfInterfaceSettings p2mpOspf =
        ospfSettings.setNetworkType(OspfNetworkType.POINT_TO_MULTIPOINT).build();
    Interface p2mpIface = ifaceBuilder.setOspfSettings(p2mpOspf).build();
    assertThat(
        getIfaceAddressesForIntraAreaRoutes(p2mpIface).collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(primarySlash32, secondarySlash32));

    // Loopback (not P2MP): Should return all interface addresses converted to /32s
    OspfInterfaceSettings broadcastOspf =
        ospfSettings.setNetworkType(OspfNetworkType.BROADCAST).build();
    Interface loopback =
        ifaceBuilder.setType(InterfaceType.LOOPBACK).setOspfSettings(broadcastOspf).build();
    assertThat(
        getIfaceAddressesForIntraAreaRoutes(loopback).collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(primarySlash32, secondarySlash32));

    // P2P loopback: Should return original interface addresses
    OspfInterfaceSettings p2pOspf =
        ospfSettings.setNetworkType(OspfNetworkType.POINT_TO_POINT).build();
    Interface p2pLoopback = ifaceBuilder.setOspfSettings(p2pOspf).build();
    assertThat(
        getIfaceAddressesForIntraAreaRoutes(p2pLoopback).collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(primary, secondary));
  }

  @Test
  public void testProcessIntraAreaAdvertisementSummaries() {
    RibDelta.Builder<OspfIntraAreaRoute> intraAreaDelta = RibDelta.builder();
    RibDelta.Builder<OspfInterAreaRoute> interAreaDelta = RibDelta.builder();
    Prefix notSummarized = Prefix.parse("1.1.1.1/29");
    Prefix summarized = Prefix.parse("10.1.1.1/29");
    // Not summarized
    _routingProcess.processIntraAreaAdvertisement(
        intraAreaDelta,
        interAreaDelta,
        ACTIVE_IFACE_NAME,
        10,
        RouteAdvertisement.<OspfIntraAreaRoute>builder()
            .setRoute(
                OspfIntraAreaRoute.builder()
                    .setArea(0)
                    .setNetwork(notSummarized)
                    .setNextHop(NextHopIp.of(Ip.parse("9.9.9.9")))
                    .setNonRouting(true)
                    .setMetric(1)
                    .build())
            .build());
    // Summarized by 10.0.0.0/8
    _routingProcess.processIntraAreaAdvertisement(
        intraAreaDelta,
        interAreaDelta,
        ACTIVE_IFACE_NAME,
        10,
        RouteAdvertisement.<OspfIntraAreaRoute>builder()
            .setRoute(
                OspfIntraAreaRoute.builder()
                    .setArea(0)
                    .setNetwork(summarized)
                    .setNextHop(NextHopIp.of(Ip.parse("9.9.9.9")))
                    .setNonRouting(true)
                    .setMetric(1)
                    .build())
            .build());
    // Intra-area delta has both
    assertThat(
        intraAreaDelta.build().getPrefixes().collect(ImmutableList.toImmutableList()),
        contains(notSummarized, summarized));
    // Inter-area delta has only the 1.1.1.1/29 prefix.
    assertThat(
        interAreaDelta.build().getPrefixes().collect(ImmutableList.toImmutableList()),
        contains(notSummarized));
  }
}
