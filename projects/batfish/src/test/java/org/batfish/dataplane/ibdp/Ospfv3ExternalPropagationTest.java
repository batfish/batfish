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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** Integration tests for OSPFv3 external type-2 propagation. */
public final class Ospfv3ExternalPropagationTest {

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

  private static Interface addOspfInterface(
      Node node,
      String name,
      String address,
      int cost) {
    Configuration c = node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
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
                .setNetworkType(
                    OspfNetworkType.POINT_TO_POINT)
                .build())
        .build();
  }

  private static Interface addConnectedInterface(
      Node node,
      String name,
      String address) {
    Configuration c = node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.LOOPBACK)
        .setAddress(
            ConcreteInterfaceAddress6.parse(address))
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean redistributeConnected,
      String... interfaces) {
    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterfaces(List.of(interfaces))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(0L, area))
        .setRedistributeConnected(
            redistributeConnected)
        .setVrf(
            node.getConfiguration().getDefaultVrf())
        .build();
  }

  private static void addProcessWithExternalControls(
      Node node,
      String routerId,
      boolean redistributeConnected,
      boolean redistributeStatic,
      long redistributionMetric,
      boolean defaultInformationOriginate,
      boolean defaultInformationOriginateAlways,
      long defaultInformationMetric,
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
        .setRedistributeConnected(
            redistributeConnected)
        .setRedistributeStatic(
            redistributeStatic)
        .setRedistributionMetric(
            redistributionMetric)
        .setDefaultInformationOriginate(
            defaultInformationOriginate)
        .setDefaultInformationOriginateAlways(
            defaultInformationOriginateAlways)
        .setDefaultInformationMetric(
            defaultInformationMetric)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3ExternalType2Route6 findExternal(
      VirtualRouter vr,
      Prefix6 prefix) {
    AbstractRoute6 route =
        vr.getOspfv3Processes()
            .get("1")
            .getRoutes()
            .stream()
            .filter(
                r ->
                    r instanceof
                        Ospfv3ExternalType2Route6)
            .filter(
                r ->
                    r.getNetwork().equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType2Route6) route;
  }

  @Test
  public void testThreeRouterExternalPropagationAndWithdrawal() {
    Node n1 = TestUtils.makeIosRouter("n1");
    Node n2 = TestUtils.makeIosRouter("n2");
    Node n3 = TestUtils.makeIosRouter("n3");

    addOspfInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        10);

    addConnectedInterface(
        n1,
        "external0",
        "2001:db8:100::1/128");

    addOspfInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        20);

    addOspfInterface(
        n2,
        "eth23",
        "2001:db8:23::2/64",
        30);

    addOspfInterface(
        n3,
        "eth32",
        "2001:db8:23::3/64",
        40);

    addProcess(
        n1,
        "192.0.2.1",
        true,
        "eth12");

    addProcess(
        n2,
        "192.0.2.2",
        false,
        "eth21",
        "eth23");

    addProcess(
        n3,
        "192.0.2.3",
        false,
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

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(topology);
    vr2.initForIgpComputation(topology);
    vr3.initForIgpComputation(topology);

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
        NodeInterfacePair.of(
            "n1", "eth12"),
        NodeInterfacePair.of(
            "n2", "eth21"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "n2", "eth23"),
        NodeInterfacePair.of(
            "n3", "eth32"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 externalPrefix =
        Prefix6.parse(
            "2001:db8:100::1/128");

    Ospfv3ExternalType2Route6 n1Route =
        findExternal(vr1, externalPrefix);

    assertThat(n1Route, notNullValue());
    assertThat(
        n1Route.getMetric(),
        equalTo(25L));
    assertThat(
        n1Route.getCostToAdvertiser(),
        equalTo(0L));

    Ospfv3ExternalType2Route6 n2Route =
        findExternal(vr2, externalPrefix);

    assertThat(n2Route, notNullValue());
    assertThat(
        n2Route.getMetric(),
        equalTo(25L));
    assertThat(
        n2Route.getCostToAdvertiser(),
        equalTo(20L));
    assertThat(
        n2Route.getAdvertiser(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        n2Route.getNextHopInterface(),
        equalTo("eth21"));
    assertThat(
        n2Route.getNextHopIp(),
        equalTo(
            Ip6.parse("2001:db8:12::1")));

    Ospfv3ExternalType2Route6 n3Route =
        findExternal(vr3, externalPrefix);

    assertThat(n3Route, notNullValue());

    // E2 metric remains 25. Internal cost to the ASBR is
    // n3 eth32 (40) + n2 eth21 (20) = 60.
    assertThat(
        n3Route.getMetric(),
        equalTo(25L));
    assertThat(
        n3Route.getCostToAdvertiser(),
        equalTo(60L));
    assertThat(
        n3Route.getNextHopInterface(),
        equalTo("eth32"));
    assertThat(
        n3Route.getNextHopIp(),
        equalTo(
            Ip6.parse("2001:db8:23::2")));

    assertThat(
        vr3.getMainRib6()
            .getRoutes(externalPrefix),
        hasItem(n3Route));

    // Remove the connected route from the ASBR.
    n1.getConfiguration()
        .getAllInterfaces()
        .get("external0")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vr1.updateConnectedAndLocalRoutesForAutostateChange();

    // Reset and reconverge OSPFv3. The external LSA-equivalent route
    // should disappear all the way downstream.
    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findExternal(vr1, externalPrefix),
        nullValue());
    assertThat(
        findExternal(vr2, externalPrefix),
        nullValue());
    assertThat(
        findExternal(vr3, externalPrefix),
        nullValue());

    assertThat(
        vr3.getMainRib6()
            .getRoutes(externalPrefix),
        empty());
  }
  @Test
  public void testStaticRedistributionAndWithdrawal() {
    Node n1 =
        TestUtils.makeIosRouter("n1");
    Node n2 =
        TestUtils.makeIosRouter("n2");

    addOspfInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        10);

    addOspfInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        20);

    Prefix6 staticPrefix =
        Prefix6.parse(
            "2001:db8:200::/64");

    StaticRoute6 staticRoute =
        StaticRoute6.builder()
            .setNetwork(staticPrefix)
            .setNextHopInterface(
                Interface.NULL_INTERFACE_NAME)
            .setTag(123L)
            .build();

    n1.getConfiguration()
        .getDefaultVrf()
        .getStaticRoutes6()
        .add(staticRoute);

    addProcessWithExternalControls(
        n1,
        "192.0.2.1",
        false,
        true,
        37L,
        false,
        false,
        Ospfv3Process.DEFAULT_INFORMATION_METRIC,
        "eth12");

    addProcess(
        n2,
        "192.0.2.2",
        false,
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

    Map<String, Node> nodes =
        ImmutableMap.of(
            "n1", n1,
            "n2", n2);

    List<VirtualRouter> vrs =
        List.of(vr1, vr2);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Ospfv3ExternalType2Route6 localAdvertisement =
        findExternal(
            vr1,
            staticPrefix);

    assertThat(
        localAdvertisement,
        notNullValue());

    assertThat(
        localAdvertisement.getMetric(),
        equalTo(37L));

    assertThat(
        localAdvertisement.getTag(),
        equalTo(123L));

    /*
     * The source static remains the local forwarding route. The router's
     * own redistributed OSPF advertisement must not compete with it.
     */
    assertThat(
        vr1.getMainRib6()
            .getRoutes(staticPrefix)
            .stream()
            .anyMatch(
                r ->
                    r instanceof StaticRoute6),
        equalTo(true));

    assertThat(
        vr1.getMainRib6()
            .getRoutes(staticPrefix)
            .stream()
            .anyMatch(
                r ->
                    r instanceof
                        Ospfv3ExternalType2Route6),
        equalTo(false));

    Ospfv3ExternalType2Route6 learned =
        findExternal(
            vr2,
            staticPrefix);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getMetric(),
        equalTo(37L));

    assertThat(
        learned.getTag(),
        equalTo(123L));

    assertThat(
        learned.getAdvertiser(),
        equalTo(
            Ip.parse("192.0.2.1")));

    /*
     * Remove the source static. Re-convergence must withdraw the
     * advertisement downstream.
     */
    n1.getConfiguration()
        .getDefaultVrf()
        .getStaticRoutes6()
        .clear();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findExternal(
            vr1,
            staticPrefix),
        nullValue());

    assertThat(
        findExternal(
            vr2,
            staticPrefix),
        nullValue());
  }

  @Test
  public void testConditionalDefaultInformationOriginate() {
    Node n1 =
        TestUtils.makeIosRouter("n1");
    Node n2 =
        TestUtils.makeIosRouter("n2");

    addOspfInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        10);

    addOspfInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        20);

    StaticRoute6 defaultRoute =
        StaticRoute6.builder()
            .setNetwork(Prefix6.ZERO)
            .setNextHopInterface(
                Interface.NULL_INTERFACE_NAME)
            .build();

    n1.getConfiguration()
        .getDefaultVrf()
        .getStaticRoutes6()
        .add(defaultRoute);

    addProcessWithExternalControls(
        n1,
        "192.0.2.1",
        false,
        false,
        Ospfv3Process.DEFAULT_REDISTRIBUTION_METRIC,
        true,
        false,
        7L,
        "eth12");

    addProcess(
        n2,
        "192.0.2.2",
        false,
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

    Map<String, Node> nodes =
        ImmutableMap.of(
            "n1", n1,
            "n2", n2);

    List<VirtualRouter> vrs =
        List.of(vr1, vr2);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Ospfv3ExternalType2Route6 learned =
        findExternal(
            vr2,
            Prefix6.ZERO);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getMetric(),
        equalTo(7L));

    /*
     * n1's local route remains STATIC, not its own OSPF advertisement.
     */
    assertThat(
        vr1.getMainRib6()
            .getRoutes(Prefix6.ZERO)
            .stream()
            .anyMatch(
                r ->
                    r.getProtocol()
                        == RoutingProtocol.STATIC),
        equalTo(true));

    assertThat(
        vr1.getMainRib6()
            .getRoutes(Prefix6.ZERO)
            .stream()
            .anyMatch(
                r ->
                    r.getProtocol()
                        == RoutingProtocol.OSPF3),
        equalTo(false));

    /*
     * Conditional origination stops when the local default disappears.
     */
    n1.getConfiguration()
        .getDefaultVrf()
        .getStaticRoutes6()
        .clear();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findExternal(
            vr2,
            Prefix6.ZERO),
        nullValue());
  }

  @Test
  public void testDefaultInformationOriginateAlways() {
    Node n1 =
        TestUtils.makeIosRouter("n1");
    Node n2 =
        TestUtils.makeIosRouter("n2");

    addOspfInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        10);

    addOspfInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        20);

    addProcessWithExternalControls(
        n1,
        "192.0.2.1",
        false,
        false,
        Ospfv3Process.DEFAULT_REDISTRIBUTION_METRIC,
        true,
        true,
        9L,
        "eth12");

    addProcess(
        n2,
        "192.0.2.2",
        false,
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

    Ospfv3ExternalType2Route6 learned =
        findExternal(
            vr2,
            Prefix6.ZERO);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getMetric(),
        equalTo(9L));

    /*
     * "always" is a control-plane advertisement. It must not fabricate
     * a forwarding default on the originating router.
     */
    assertThat(
        vr1.getMainRib6()
            .getRoutes(Prefix6.ZERO),
        empty());

    assertThat(
        vr2.getMainRib6()
            .getRoutes(Prefix6.ZERO),
        hasItem(learned));
  }

}
