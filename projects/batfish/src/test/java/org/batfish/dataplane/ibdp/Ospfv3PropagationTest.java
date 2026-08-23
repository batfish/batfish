package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 intra-area IPv6 propagation. */
public final class Ospfv3PropagationTest {

  private static final class TestL3Adjacencies
      implements L3Adjacencies {

    private final Map<
            NodeInterfacePair, NodeInterfacePair>
        _pairs = new HashMap<>();

    void addPair(
        NodeInterfacePair lhs,
        NodeInterfacePair rhs) {
      _pairs.put(lhs, rhs);
      _pairs.put(rhs, lhs);
    }

    @Override
    public boolean inSameBroadcastDomain(
        NodeInterfacePair i1,
        NodeInterfacePair i2) {
      return Optional.ofNullable(_pairs.get(i1))
          .map(i2::equals)
          .orElse(false);
    }

    @Override
    public Optional<NodeInterfacePair>
        pairedPointToPointL3Interface(
            NodeInterfacePair iface) {
      return Optional.ofNullable(_pairs.get(iface));
    }
  }

  private static Node makeRouterWithBlueVrf(
      String hostname) {
    Configuration c =
        Configuration.builder()
            .setHostname(hostname)
            .setConfigurationFormat(
                ConfigurationFormat.CISCO_IOS)
            .build();

    Vrf.builder()
        .setName(
            Configuration.DEFAULT_VRF_NAME)
        .setOwner(c)
        .build();

    Vrf.builder()
        .setName("BLUE")
        .setOwner(c)
        .build();

    return new Node(c);
  }

  private static Interface addInterfaceInVrf(
      Node node,
      String vrfName,
      String name,
      String address,
      InterfaceType type,
      int cost,
      OspfNetworkType networkType) {
    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(
            c.getVrfs().get(vrfName))
        .setType(type)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .setBandwidth(
            10_000_000_000D)
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(0L)
                .setCost(cost)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(networkType)
                .build())
        .build();
  }

  private static void addProcessInVrf(
      Node node,
      String vrfName,
      String routerId,
      String... interfaces) {
    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterfaces(
                List.of(interfaces))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(
                0L, area))
        .setVrf(
            node.getConfiguration()
                .getVrfs()
                .get(vrfName))
        .build();
  }

  private static Interface addInterface(
      Node node,
      String name,
      String address,
      InterfaceType type,
      int cost,
      OspfNetworkType networkType) {
    Configuration c = node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(type)
        .setAddress(
            ConcreteInterfaceAddress6.parse(address))
        .setBandwidth(10_000_000_000D)
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(0L)
                .setCost(cost)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(networkType)
                .build())
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      String... interfaces) {
    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterfaces(List.of(interfaces))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(Ip.parse(routerId))
        .setAreas(ImmutableMap.of(0L, area))
        .setVrf(
            node.getConfiguration().getDefaultVrf())
        .build();
  }

  private static void addProcessWithStateAndDistances(
      Node node,
      String routerId,
      boolean enabled,
      int intraAreaDistance,
      int interAreaDistance,
      int externalDistance,
      String... interfaces) {

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterfaces(
                List.of(interfaces))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(
                0L, area))
        .setAdminCost(
            intraAreaDistance)
        .setInterAreaAdminCost(
            interAreaDistance)
        .setExternalAdminCost(
            externalDistance)
        .setEnabled(enabled)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3IntraAreaRoute6 findIntraRoute(
      VirtualRouter vr, Prefix6 prefix) {
    AbstractRoute6 route =
        vr.getOspfv3Processes()
            .get("1")
            .getRoutes()
            .stream()
            .filter(
                r ->
                    r instanceof
                        Ospfv3IntraAreaRoute6)
            .filter(
                r ->
                    r.getNetwork().equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3IntraAreaRoute6) route;
  }

  @Test
  public void testThreeRouterPropagationAndWithdrawal() {
    Node n1 = TestUtils.makeIosRouter("n1");
    Node n2 = TestUtils.makeIosRouter("n2");
    Node n3 = TestUtils.makeIosRouter("n3");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL,
        10,
        OspfNetworkType.POINT_TO_POINT);

    addInterface(
        n1,
        "loopback0",
        "2001:db8:1::1/128",
        InterfaceType.LOOPBACK,
        1,
        OspfNetworkType.BROADCAST);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL,
        20,
        OspfNetworkType.POINT_TO_POINT);

    addInterface(
        n2,
        "eth23",
        "2001:db8:23::2/64",
        InterfaceType.PHYSICAL,
        30,
        OspfNetworkType.POINT_TO_POINT);

    addInterface(
        n3,
        "eth32",
        "2001:db8:23::3/64",
        InterfaceType.PHYSICAL,
        40,
        OspfNetworkType.POINT_TO_POINT);

    addProcess(
        n1,
        "192.0.2.1",
        "eth12",
        "loopback0");

    addProcess(
        n2,
        "192.0.2.2",
        "eth21",
        "eth23");

    addProcess(
        n3,
        "192.0.2.3",
        "eth32");

    VirtualRouter vr1 =
        n1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);
    VirtualRouter vr2 =
        n2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);
    VirtualRouter vr3 =
        n3.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext emptyTopology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(emptyTopology);
    vr2.initForIgpComputation(emptyTopology);
    vr3.initForIgpComputation(emptyTopology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "n1", n1,
            "n2", n2,
            "n3", n3);

    List<VirtualRouter> vrs =
        List.of(vr1, vr2, vr3);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of("n1", "eth12"),
        NodeInterfacePair.of("n2", "eth21"));

    adjacencies.addPair(
        NodeInterfacePair.of("n2", "eth23"),
        NodeInterfacePair.of("n3", "eth32"));

    int iterations =
        IncrementalBdpEngine
            .initOspfv3InternalRoutes(
                nodes, vrs, adjacencies);

    assertThat(iterations > 0, equalTo(true));

    Prefix6 loopbackPrefix =
        Prefix6.parse("2001:db8:1::1/128");

    Ospfv3IntraAreaRoute6 n2Route =
        findIntraRoute(vr2, loopbackPrefix);

    assertThat(n2Route, notNullValue());
    assertThat(n2Route.getMetric(), equalTo(21L));
    assertThat(
        n2Route.getNextHopInterface(),
        equalTo("eth21"));
    assertThat(
        n2Route.getNextHopIp(),
        equalTo(Ip6.parse("2001:db8:12::1")));

    Ospfv3IntraAreaRoute6 n3Route =
        findIntraRoute(vr3, loopbackPrefix);

    assertThat(n3Route, notNullValue());

    // n1 loopback cost 1 + n2->n1 cost 20 + n3->n2 cost 40.
    assertThat(n3Route.getMetric(), equalTo(61L));
    assertThat(
        n3Route.getNextHopInterface(),
        equalTo("eth32"));
    assertThat(
        n3Route.getNextHopIp(),
        equalTo(Ip6.parse("2001:db8:23::2")));

    assertThat(
        vr3.getMainRib6().getRoutes(loopbackPrefix),
        hasItem(n3Route));

    // Break the n2-n3 adjacency and rebuild connected/OSPFv3 state.
    n2.getConfiguration()
        .getAllInterfaces()
        .get("eth23")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vr2.updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes, vrs, adjacencies);

    // n3 must withdraw the route learned through n2.
    assertThat(
        findIntraRoute(vr3, loopbackPrefix),
        nullValue());

    // The stale learned OSPFv3 candidate must also be gone from main RIB6.
    assertThat(
        vr3.getMainRib6().getRoutes(loopbackPrefix),
        empty());
  }

  @Test
  public void testSettingsCompatibility() {
    Ospfv3InterfaceSettings baseline =
        Ospfv3InterfaceSettings.builder()
            .setAreaName(0L)
            .setProcess("1")
            .setEnabled(true)
            .setPassive(false)
            .setHelloInterval(10)
            .setDeadInterval(40)
            .setNetworkType(
                OspfNetworkType.POINT_TO_POINT)
            .build();

    Ospfv3InterfaceSettings compatible =
        Ospfv3InterfaceSettings.builder()
            .setAreaName(0L)
            // Process IDs are locally significant and need not match.
            .setProcess("99")
            .setEnabled(true)
            .setPassive(false)
            .setHelloInterval(10)
            .setDeadInterval(40)
            .setNetworkType(
                OspfNetworkType.POINT_TO_POINT)
            .build();

    assertThat(
        Ospfv3RoutingProcess
            .areInterfaceSettingsCompatible(
                baseline, compatible),
        equalTo(true));

    Ospfv3InterfaceSettings wrongArea =
        Ospfv3InterfaceSettings.builder()
            .setAreaName(1L)
            .setProcess("99")
            .setEnabled(true)
            .setPassive(false)
            .setHelloInterval(10)
            .setDeadInterval(40)
            .setNetworkType(
                OspfNetworkType.POINT_TO_POINT)
            .build();

    assertThat(
        Ospfv3RoutingProcess
            .areInterfaceSettingsCompatible(
                baseline, wrongArea),
        equalTo(false));

    Ospfv3InterfaceSettings wrongTimers =
        Ospfv3InterfaceSettings.builder()
            .setAreaName(0L)
            .setProcess("99")
            .setEnabled(true)
            .setPassive(false)
            .setHelloInterval(5)
            .setDeadInterval(20)
            .setNetworkType(
                OspfNetworkType.POINT_TO_POINT)
            .build();

    assertThat(
        Ospfv3RoutingProcess
            .areInterfaceSettingsCompatible(
                baseline, wrongTimers),
        equalTo(false));
  }
  @Test
  public void testNamedVrfPropagation() {
    Node n1 =
        makeRouterWithBlueVrf("n1");
    Node n2 =
        makeRouterWithBlueVrf("n2");

    addInterfaceInVrf(
        n1,
        "BLUE",
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL,
        10,
        OspfNetworkType.POINT_TO_POINT);

    addInterfaceInVrf(
        n1,
        "BLUE",
        "loopback0",
        "2001:db8:100::1/128",
        InterfaceType.LOOPBACK,
        1,
        OspfNetworkType.BROADCAST);

    addInterfaceInVrf(
        n2,
        "BLUE",
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL,
        20,
        OspfNetworkType.POINT_TO_POINT);

    addProcessInVrf(
        n1,
        "BLUE",
        "192.0.2.1",
        "eth12",
        "loopback0");

    addProcessInVrf(
        n2,
        "BLUE",
        "192.0.2.2",
        "eth21");

    VirtualRouter vr1 =
        n1.getVirtualRouterOrThrow(
            "BLUE");

    VirtualRouter vr2 =
        n2.getVirtualRouterOrThrow(
            "BLUE");

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(
        topology);
    vr2.initForIgpComputation(
        topology);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "n1", "eth12"),
        NodeInterfacePair.of(
            "n2", "eth21"));

    int iterations =
        IncrementalBdpEngine
            .initOspfv3InternalRoutes(
                ImmutableMap.of(
                    "n1", n1,
                    "n2", n2),
                List.of(vr1, vr2),
                adjacencies);

    assertThat(
        iterations > 0,
        equalTo(true));

    Prefix6 loopback =
        Prefix6.parse(
            "2001:db8:100::1/128");

    Ospfv3IntraAreaRoute6 learned =
        findIntraRoute(
            vr2,
            loopback);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getNextHopInterface(),
        equalTo("eth21"));

    assertThat(
        learned.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:12::1")));

    assertThat(
        vr2.getMainRib6()
            .getRoutes(loopback),
        hasItem(learned));

    /*
     * The route/process belongs only to BLUE. The default routing
     * table remains independent.
     */
    assertThat(
        n1.getConfiguration()
            .getDefaultVrf()
            .getOspfv3Processes()
            .isEmpty(),
        equalTo(true));

    assertThat(
        n2.getConfiguration()
            .getDefaultVrf()
            .getOspfv3Processes()
            .isEmpty(),
        equalTo(true));
  }

  @Test
  public void testIntraAreaAdministrativeDistance() {
    Node n1 =
        TestUtils.makeIosRouter("n1");
    Node n2 =
        TestUtils.makeIosRouter("n2");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL,
        10,
        OspfNetworkType.POINT_TO_POINT);

    addInterface(
        n1,
        "loopback0",
        "2001:db8:100::1/128",
        InterfaceType.LOOPBACK,
        1,
        OspfNetworkType.BROADCAST);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL,
        20,
        OspfNetworkType.POINT_TO_POINT);

    addProcess(
        n1,
        "192.0.2.1",
        "eth12",
        "loopback0");

    addProcessWithStateAndDistances(
        n2,
        "192.0.2.2",
        true,
        222,
        223,
        224,
        "eth21");

    VirtualRouter vr1 =
        n1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr2 =
        n2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(topology);
    vr2.initForIgpComputation(topology);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "n1", "eth12"),
        NodeInterfacePair.of(
            "n2", "eth21"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            List.of(
                vr1, vr2),
            adjacencies);

    Ospfv3IntraAreaRoute6 learned =
        findIntraRoute(
            vr2,
            Prefix6.parse(
                "2001:db8:100::1/128"));

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getAdministrativeCost(),
        equalTo(222L));

    assertThat(
        vr2.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:100::1/128")),
        hasItem(learned));
  }

  @Test
  public void testDisabledOspfv3ProcessSuppressesRouting() {
    Node n1 =
        TestUtils.makeIosRouter("n1");
    Node n2 =
        TestUtils.makeIosRouter("n2");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL,
        10,
        OspfNetworkType.POINT_TO_POINT);

    addInterface(
        n1,
        "loopback0",
        "2001:db8:100::1/128",
        InterfaceType.LOOPBACK,
        1,
        OspfNetworkType.BROADCAST);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL,
        20,
        OspfNetworkType.POINT_TO_POINT);

    addProcess(
        n1,
        "192.0.2.1",
        "eth12",
        "loopback0");

    addProcessWithStateAndDistances(
        n2,
        "192.0.2.2",
        false,
        110,
        110,
        110,
        "eth21");

    VirtualRouter vr1 =
        n1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr2 =
        n2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(topology);
    vr2.initForIgpComputation(topology);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "n1", "eth12"),
        NodeInterfacePair.of(
            "n2", "eth21"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            List.of(
                vr1, vr2),
            adjacencies);

    Prefix6 loopback =
        Prefix6.parse(
            "2001:db8:100::1/128");

    assertThat(
        findIntraRoute(
            vr2,
            loopback),
        nullValue());

    assertThat(
        vr2.getOspfv3Processes()
            .get("1")
            .getRoutes(),
        empty());

    assertThat(
        vr2.getMainRib6()
            .getRoutes(loopback),
        empty());
  }

}
