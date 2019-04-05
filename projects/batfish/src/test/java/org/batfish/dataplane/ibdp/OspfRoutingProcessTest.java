package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasAdministrativeCost;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.isNonRouting;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.computeDefaultInterAreaRouteToInject;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.convertAndFilterIntraAreaRoutesToPropagate;
import static org.batfish.dataplane.ibdp.OspfRoutingProcess.filterInterAreaRoutesToPropagateAtABR;
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
import static org.hamcrest.Matchers.instanceOf;
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
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfNeighborConfig;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfProcess.Builder;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link OspfRoutingProcess} */
public class OspfRoutingProcessTest {
  private static final InterfaceAddress INACTIVE_ADDR = new InterfaceAddress("1.1.1.1/24");
  private static final InterfaceAddress ACTIVE_ADDR_1 = new InterfaceAddress("2.2.2.2/24");
  private static final InterfaceAddress ACTIVE_ADDR_2 = new InterfaceAddress("3.3.3.3/24");
  private static final InterfaceAddress PASSIVE_ADDR = new InterfaceAddress("4.4.4.4/24");
  private static final InterfaceAddress OSPF_DISABLED_ADDR = new InterfaceAddress("5.5.5.5/24");
  private static final String HOSTNAME = "r1";
  private static final String VRF_NAME = "vrf";
  private static OspfArea AREA0_CONFIG;
  private final OspfTopology _emptyOspfTopology =
      new OspfTopology(ValueGraphBuilder.directed().build());
  private OspfRoutingProcess _routingProcess;
  private Configuration _c;
  // Non-default, distinctive costs
  private static final Map<RoutingProtocol, Integer> ADMIN_COSTS =
      ImmutableMap.of(
          RoutingProtocol.OSPF,
          100,
          RoutingProtocol.OSPF_IA,
          200,
          RoutingProtocol.OSPF_E1,
          300,
          RoutingProtocol.OSPF_E2,
          400);

  @Before
  public void setUp() {
    NetworkFactory nf = new NetworkFactory();
    _c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(HOSTNAME)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(VRF_NAME).build();
    Interface.Builder ib =
        nf.interfaceBuilder()
            .setVrf(vrf)
            .setOwner(_c)
            .setOspfProcess("1")
            .setOspfPointToPoint(true)
            .setOspfEnabled(true)
            .setType(InterfaceType.PHYSICAL)
            .setBandwidth(1e8);
    Interface inactiveIface =
        ib.setActive(false).setName("inactive").setAddress(INACTIVE_ADDR).setOspfCost(10).build();
    ib.setActive(true);
    Interface activeIface =
        ib.setName("active")
            .setAddresses(ACTIVE_ADDR_1, ACTIVE_ADDR_2)
            .setOspfPointToPoint(true)
            .build();
    Interface passiveIface =
        ib.setName("passive")
            .setAddress(PASSIVE_ADDR)
            .setOspfPassive(true)
            .setOspfPointToPoint(false)
            .build();
    Interface ospfDisabled =
        ib.setName("ospfDisabled")
            .setAddress(OSPF_DISABLED_ADDR)
            .setOspfPassive(false)
            .setOspfPointToPoint(false)
            .setOspfEnabled(false)
            .build();
    AREA0_CONFIG =
        OspfArea.builder(nf)
            .setNumber(0)
            .setInterfaces(
                ImmutableSet.of(
                    inactiveIface.getName(),
                    activeIface.getName(),
                    passiveIface.getName(),
                    ospfDisabled.getName(),
                    "NonExistent"))
            .build();

    OspfProcess ospfProcess =
        nf.ospfProcessBuilder()
            .setProcessId("1")
            .setReferenceBandwidth(10e9)
            .setVrf(vrf)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG))
            .setAdminCosts(ADMIN_COSTS)
            .setNeighbors(
                ImmutableMap.of(
                    "active",
                    OspfNeighborConfig.builder()
                        .setHostname(HOSTNAME)
                        .setVrfName(VRF_NAME)
                        .setInterfaceName("active")
                        .setArea(0L)
                        .build()))
            .build();
    _routingProcess = new OspfRoutingProcess(ospfProcess, VRF_NAME, _c, _emptyOspfTopology);
  }

  private OspfTopology nonEmptyOspfTopology() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().build();
    OspfNeighborConfigId c1 = new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", "active");
    OspfNeighborConfigId c2 = new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface");
    // Add edges both directions
    graph.putEdgeValue(
        c1,
        c2,
        new OspfSessionProperties(
            0L, new IpLink(ACTIVE_ADDR_1.getIp(), Ip.create(ACTIVE_ADDR_1.getIp().asLong() + 1))));
    graph.putEdgeValue(
        c2,
        c1,
        new OspfSessionProperties(
            0L, new IpLink(Ip.create(ACTIVE_ADDR_1.getIp().asLong() + 1), ACTIVE_ADDR_1.getIp())));
    return new OspfTopology(graph);
  }

  /** Check that a new (but un-initialized) process is not dirty */
  @Test
  public void testNotDirtyOnCreation() {
    assertFalse(_routingProcess.isDirty());
  }

  @Test
  public void testDirtyPostInitialization() {
    _routingProcess.initialize();
    assertTrue(_routingProcess.isDirty());
  }

  /**
   * Ensure that if no new incoming messages are supplied, one iteration is sufficient to return to
   * non-dirty state
   */
  @Test
  public void testNotDirtyAfterOneIteration() {
    _routingProcess.initialize();
    // Empty map in this particular case just means no valid neighbors.
    _routingProcess.executeIteration(ImmutableMap.of());

    // Initialization delta should have been cleared
    assertFalse(_routingProcess.isDirty());
  }

  @Test
  public void testInitializeRoutesByArea() {
    // Must not crash on non-existent interface
    RibDelta<OspfIntraAreaRoute> delta = _routingProcess.initializeRoutesByArea(AREA0_CONFIG);
    // All routes were added
    assertTrue(delta.getActions().stream().allMatch(r -> r.getReason() == ADD));

    /*
      Requirements:
    - Must skip disabled interface
    - Must include OSPF passive interface
    - Must include all addresses from active interface
    */
    assertThat(
        delta.getRoutes(),
        containsInAnyOrder(
            hasPrefix(PASSIVE_ADDR.getPrefix()),
            hasPrefix(ACTIVE_ADDR_1.getPrefix()),
            hasPrefix(ACTIVE_ADDR_2.getPrefix())));
  }

  @Test
  public void testGetIncrementalCost() {
    // Default behavior
    assertThat(_routingProcess.getIncrementalCost("active", false), equalTo(10L));

    // Overriden behavior for transit links
    final long overrideMetric = 8888L;
    Builder ospfProcess =
        OspfProcess.builder()
            .setProcessId("1")
            .setReferenceBandwidth(10e9)
            .setAreas(ImmutableSortedMap.of(0L, AREA0_CONFIG))
            .setMaxMetricTransitLinks(overrideMetric);

    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost("active", false),
        equalTo(overrideMetric));

    // Overriden behavior for stub links
    ospfProcess.setMaxMetricTransitLinks(null).setMaxMetricStubNetworks(overrideMetric);
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost("active", false),
        equalTo(10L));
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost("active", true),
        equalTo(overrideMetric));
    assertThat(
        new OspfRoutingProcess(ospfProcess.build(), VRF_NAME, _c, _emptyOspfTopology)
            .getIncrementalCost("passive", false),
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
  public void testUpdateTopology() {
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    OspfNeighborConfigId n1 = new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", "active");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface");

    // Both of these should not crash because new message queues exist now
    _routingProcess.enqueueMessagesIntra(OspfTopology.makeEdge(n2, n1), ImmutableSet.of());
    _routingProcess.enqueueMessagesInter(OspfTopology.makeEdge(n2, n1), ImmutableSet.of());
  }

  @Test
  public void updateTopologyIsNonDestructive() {
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    OspfNeighborConfigId n1 = new OspfNeighborConfigId(HOSTNAME, VRF_NAME, "1", "active");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("r2", VRF_NAME, "1", "someIface");
    _routingProcess.enqueueMessagesIntra(
        OspfTopology.makeEdge(n2, n1),
        ImmutableSet.of(
            new RouteAdvertisement<>(
                OspfIntraAreaRoute.builder()
                    .setArea(0)
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("8.8.8.8"))
                    .build())));
    // Re-update topology
    _routingProcess.updateTopology(nonEmptyOspfTopology());
    // Ensure still in dirty state (didn't lose the queued advertisement)
    assertTrue(_routingProcess.isDirty());
  }

  @Test
  public void testProcessIntraAreaRouteOnImport() {
    RouteAdvertisement<OspfIntraAreaRoute> transformed =
        _routingProcess.transformIntraAreaRouteOnImport(
            new RouteAdvertisement<>(
                OspfIntraAreaRoute.builder()
                    .setArea(0)
                    .setNetwork(Prefix.ZERO)
                    .setMetric(1)
                    .setNextHopIp(Ip.parse("8.8.8.8"))
                    .build()),
            10);

    // Update metric, admin
    assertThat(transformed.getRoute(), allOf(hasMetric(11L), hasAdministrativeCost(equalTo(100))));
  }

  @Test
  public void testProcessInterAreaRouteOnImport() {
    RouteAdvertisement<OspfInterAreaRoute> transformed =
        _routingProcess
            .transformInterAreaRouteOnImport(
                new RouteAdvertisement<>(
                    OspfInterAreaRoute.builder()
                        .setArea(0)
                        .setNetwork(Prefix.ZERO)
                        .setMetric(1)
                        .setNonRouting(true)
                        .setNextHopIp(Ip.parse("8.8.8.8"))
                        .build()),
                10)
            .get();

    // Update metric, admin, clear non-routing bit
    assertThat(
        transformed.getRoute(),
        allOf(hasMetric(11L), hasAdministrativeCost(equalTo(200)), isNonRouting(false)));
  }

  @Test
  public void testConvertAndFilterIntraAreaRoutesToPropagate() {
    OspfIntraAreaRoute route0 =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();
    OspfIntraAreaRoute route1 =
        OspfIntraAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfIntraAreaRoute> delta =
        RibDelta.<OspfIntraAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    List<RouteAdvertisement<OspfInterAreaRoute>> transformed =
        convertAndFilterIntraAreaRoutesToPropagate(delta, AREA0_CONFIG, null, nextHopIp, null)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route1.getMetric())))));

    // Regular conversion but with custom metric
    transformed =
        convertAndFilterIntraAreaRoutesToPropagate(delta, AREA0_CONFIG, null, nextHopIp, 999L)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(999L)))));

    // Convert and filter with route filter list
    transformed =
        convertAndFilterIntraAreaRoutesToPropagate(
                delta,
                AREA0_CONFIG,
                new RouteFilterList(
                    "filter",
                    ImmutableList.of(
                        new RouteFilterLine(
                            LineAction.DENY, Prefix.parse("1.0.0.0/8"), new SubRange(8, 8)))),
                nextHopIp,
                999L)
            .collect(Collectors.toList());
    assertThat(transformed, empty());

    // Convert but filter because STUB and suppress type 3
    transformed =
        convertAndFilterIntraAreaRoutesToPropagate(
                delta,
                OspfArea.builder()
                    .setStub(StubSettings.builder().setSuppressType3(true).build())
                    .setNumber(2)
                    .build(),
                null,
                nextHopIp,
                null)
            .collect(Collectors.toList());
    assertThat(transformed, empty());

    // Convert but filter because NSSA and suppress type 3
    transformed =
        convertAndFilterIntraAreaRoutesToPropagate(
                delta,
                OspfArea.builder()
                    .setNssa(NssaSettings.builder().setSuppressType3(true).build())
                    .setNumber(2)
                    .build(),
                null,
                nextHopIp,
                null)
            .collect(Collectors.toList());
    assertThat(transformed, empty());
  }

  @Test
  public void testFilterInterAreaRoutesToPropagateAtABR() {
    OspfInterAreaRoute route0 =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();
    OspfInterAreaRoute route1 =
        OspfInterAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfInterAreaRoute> delta =
        RibDelta.<OspfInterAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    List<RouteAdvertisement<OspfInterAreaRoute>> transformed =
        filterInterAreaRoutesToPropagateAtABR(delta, AREA0_CONFIG, null, nextHopIp, null)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route1.getMetric())))));

    // Regular conversion but with custom metric
    transformed =
        filterInterAreaRoutesToPropagateAtABR(delta, AREA0_CONFIG, null, nextHopIp, 999L)
            .collect(Collectors.toList());
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route1.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(999L)))));

    // Convert and filter with route filter list
    transformed =
        filterInterAreaRoutesToPropagateAtABR(
                delta,
                AREA0_CONFIG,
                new RouteFilterList(
                    "filter",
                    ImmutableList.of(
                        new RouteFilterLine(
                            LineAction.DENY, Prefix.parse("1.0.0.0/8"), new SubRange(8, 8)))),
                nextHopIp,
                999L)
            .collect(Collectors.toList());
    assertThat(transformed, empty());

    // Convert but filter because STUB and suppress type 3
    transformed =
        filterInterAreaRoutesToPropagateAtABR(
                delta,
                OspfArea.builder()
                    .setStub(StubSettings.builder().setSuppressType3(true).build())
                    .setNumber(2)
                    .build(),
                null,
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
                null,
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
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();
    OspfInterAreaRoute route1 =
        OspfInterAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfInterAreaRoute> delta =
        RibDelta.<OspfInterAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    Collection<RouteAdvertisement<OspfInterAreaRoute>> transformed =
        transformInterAreaRoutesOnExportNonABR(delta, AREA0_CONFIG, nextHopIp, null);
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route0.getMetric())))));

    // Regular conversion but with custom metric
    transformed = transformInterAreaRoutesOnExportNonABR(delta, AREA0_CONFIG, nextHopIp, 999L);
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfInterAreaRoute.class),
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(999L)))));
  }

  @Test
  public void testTransformIntraAreaRoutesOnExport() {
    OspfIntraAreaRoute route0 =
        OspfIntraAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(1)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();
    OspfIntraAreaRoute route1 =
        OspfIntraAreaRoute.builder()
            .setArea(1)
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setMetric(42)
            .setNextHopIp(Ip.parse("8.8.8.8"))
            .build();

    final Ip nextHopIp = Ip.parse("9.9.9.9");
    final RibDelta<OspfIntraAreaRoute> delta =
        RibDelta.<OspfIntraAreaRoute>builder().add(route0).add(route1).build();

    // Regular conversion
    Collection<RouteAdvertisement<OspfIntraAreaRoute>> transformed =
        transformIntraAreaRoutesOnExport(delta, AREA0_CONFIG, nextHopIp);
    assertThat(
        transformed,
        contains(
            hasRoute(
                allOf(
                    instanceOf(OspfIntraAreaRoute.class),
                    hasPrefix(route0.getNetwork()),
                    hasNextHopIp(equalTo(nextHopIp)),
                    hasMetric(route0.getMetric())))));
  }
}
