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
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** Integration tests for OSPFv3 ABR and inter-area propagation. */
public final class Ospfv3InterAreaPropagationTest {

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
      return Optional.ofNullable(
          _pairs.get(iface));
    }
  }

  private static Interface addInterface(
      Node node,
      String name,
      String address,
      long area,
      int cost,
      InterfaceType type) {

    Configuration c =
        node.getConfiguration();

    boolean loopback =
        type == InterfaceType.LOOPBACK;

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(type)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .setBandwidth(
            10_000_000_000D)
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(area)
                .setCost(cost)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(loopback)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    loopback
                        ? OspfNetworkType.BROADCAST
                        : OspfNetworkType.POINT_TO_POINT)
                .build())
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      Map<Long, List<String>> areas) {

    ImmutableMap.Builder<Long, Ospfv3Area>
        areaBuilder =
            ImmutableMap.builder();

    areas.forEach(
        (number, interfaces) ->
            areaBuilder.put(
                number,
                Ospfv3Area.builder()
                    .setNumber(number)
                    .addInterfaces(interfaces)
                    .build()));

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areaBuilder.build())
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3IntraAreaRoute6
      findIntra(
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
                        Ospfv3IntraAreaRoute6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3IntraAreaRoute6) route;
  }

  private static Ospfv3InterAreaRoute6
      findInter(
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
                        Ospfv3InterAreaRoute6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3InterAreaRoute6) route;
  }

  @Test
  public void testBackboneInterAreaPropagationAndWithdrawal() {

    Node r1 =
        TestUtils.makeIosRouter("r1");
    Node abr1 =
        TestUtils.makeIosRouter("abr1");
    Node abr2 =
        TestUtils.makeIosRouter("abr2");
    Node r2 =
        TestUtils.makeIosRouter("r2");

    // Area 1.
    addInterface(
        r1,
        "eth-r1-a1",
        "2001:db8:11::1/64",
        1L,
        5,
        InterfaceType.PHYSICAL);

    addInterface(
        r1,
        "loopback0",
        "2001:db8:1::1/128",
        1L,
        1,
        InterfaceType.LOOPBACK);

    addInterface(
        abr1,
        "eth-a1-r1",
        "2001:db8:11::2/64",
        1L,
        10,
        InterfaceType.PHYSICAL);

    // Backbone area 0 between the two ABRs.
    addInterface(
        abr1,
        "eth-a1-a2",
        "2001:db8:100::1/64",
        0L,
        15,
        InterfaceType.PHYSICAL);

    addInterface(
        abr2,
        "eth-a2-a1",
        "2001:db8:100::2/64",
        0L,
        20,
        InterfaceType.PHYSICAL);

    // Area 2.
    addInterface(
        abr2,
        "eth-a2-r2",
        "2001:db8:22::1/64",
        2L,
        25,
        InterfaceType.PHYSICAL);

    addInterface(
        r2,
        "eth-r2-a2",
        "2001:db8:22::2/64",
        2L,
        30,
        InterfaceType.PHYSICAL);

    addProcess(
        r1,
        "192.0.2.1",
        ImmutableMap.of(
            1L,
            List.of(
                "eth-r1-a1",
                "loopback0")));

    addProcess(
        abr1,
        "192.0.2.11",
        ImmutableMap.of(
            0L,
            List.of("eth-a1-a2"),
            1L,
            List.of("eth-a1-r1")));

    addProcess(
        abr2,
        "192.0.2.22",
        ImmutableMap.of(
            0L,
            List.of("eth-a2-a1"),
            2L,
            List.of("eth-a2-r2")));

    addProcess(
        r2,
        "192.0.2.2",
        ImmutableMap.of(
            2L,
            List.of("eth-r2-a2")));

    VirtualRouter vrR1 =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);
    VirtualRouter vrAbr1 =
        abr1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);
    VirtualRouter vrAbr2 =
        abr2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);
    VirtualRouter vrR2 =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vrR1.initForIgpComputation(topology);
    vrAbr1.initForIgpComputation(topology);
    vrAbr2.initForIgpComputation(topology);
    vrR2.initForIgpComputation(topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "r1", r1,
            "abr1", abr1,
            "abr2", abr2,
            "r2", r2);

    List<VirtualRouter> vrs =
        List.of(
            vrR1,
            vrAbr1,
            vrAbr2,
            vrR2);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "r1", "eth-r1-a1"),
        NodeInterfacePair.of(
            "abr1", "eth-a1-r1"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr1", "eth-a1-a2"),
        NodeInterfacePair.of(
            "abr2", "eth-a2-a1"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr2", "eth-a2-r2"),
        NodeInterfacePair.of(
            "r2", "eth-r2-a2"));

    int iterations =
        IncrementalBdpEngine
            .initOspfv3InternalRoutes(
                nodes,
                vrs,
                adjacencies);

    assertThat(
        iterations > 0,
        equalTo(true));

    Prefix6 loopback =
        Prefix6.parse(
            "2001:db8:1::1/128");

    // ABR1 learns it as an intra-area route in area 1:
    // r1 loopback metric 1 + ABR1 area-1 interface cost 10.
    Ospfv3IntraAreaRoute6 abr1Route =
        findIntra(vrAbr1, loopback);

    assertThat(
        abr1Route,
        notNullValue());
    assertThat(
        abr1Route.getArea(),
        equalTo(1L));
    assertThat(
        abr1Route.getMetric(),
        equalTo(11L));

    // ABR2 receives the summary across area 0:
    // 11 + ABR2 backbone interface cost 20 = 31.
    Ospfv3InterAreaRoute6 abr2Route =
        findInter(vrAbr2, loopback);

    assertThat(
        abr2Route,
        notNullValue());
    assertThat(
        abr2Route.getArea(),
        equalTo(0L));
    assertThat(
        abr2Route.getMetric(),
        equalTo(31L));
    assertThat(
        abr2Route.getNextHopInterface(),
        equalTo("eth-a2-a1"));
    assertThat(
        abr2Route.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:100::1")));

    // ABR2 summarizes from backbone into area 2.
    // R2 adds its own area-2 interface cost 30:
    // 31 + 30 = 61.
    Ospfv3InterAreaRoute6 r2Route =
        findInter(vrR2, loopback);

    assertThat(
        r2Route,
        notNullValue());
    assertThat(
        r2Route.getArea(),
        equalTo(2L));
    assertThat(
        r2Route.getMetric(),
        equalTo(61L));
    assertThat(
        r2Route.getNextHopInterface(),
        equalTo("eth-r2-a2"));
    assertThat(
        r2Route.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:22::1")));

    assertThat(
        vrR2.getMainRib6()
            .getRoutes(loopback),
        hasItem(r2Route));

    // Withdraw the originating area-1 loopback.
    r1.getConfiguration()
        .getAllInterfaces()
        .get("loopback0")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vrR1
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findIntra(vrAbr1, loopback),
        nullValue());

    assertThat(
        findInter(vrAbr2, loopback),
        nullValue());

    assertThat(
        findInter(vrR2, loopback),
        nullValue());

    assertThat(
        vrR2.getMainRib6()
            .getRoutes(loopback),
        empty());
  }

  @Test
  public void testNoDirectNonBackboneTransit() {

    Node abr =
        TestUtils.makeIosRouter("abr");

    addInterface(
        abr,
        "area1",
        "2001:db8:1::1/64",
        1L,
        10,
        InterfaceType.PHYSICAL);

    addInterface(
        abr,
        "area2",
        "2001:db8:2::1/64",
        2L,
        10,
        InterfaceType.PHYSICAL);

    addProcess(
        abr,
        "192.0.2.100",
        ImmutableMap.of(
            1L,
            List.of("area1"),
            2L,
            List.of("area2")));

    VirtualRouter vr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    vr.initForIgpComputation(
        TopologyContext.builder().build());

    Ospfv3RoutingProcess process =
        vr.getOspfv3Processes()
            .get("1");

    assertThat(
        process.canAdvertiseBetweenAreas(
            1L, 2L),
        equalTo(false));
  }
}
