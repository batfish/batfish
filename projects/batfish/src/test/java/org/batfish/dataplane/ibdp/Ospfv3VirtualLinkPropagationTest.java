package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.datamodel.ospf.Ospfv3VirtualLink;
import org.junit.Test;

/** End-to-end tests for OSPFv3 virtual backbone links. */
public final class Ospfv3VirtualLinkPropagationTest {

  private static final class TestL3Adjacencies
      implements L3Adjacencies {

    private final Map<
            NodeInterfacePair,
            NodeInterfacePair>
        _pairs =
            new HashMap<>();

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

      return Optional.ofNullable(
              _pairs.get(i1))
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

  private static Interface addOspfInterface(
      Node node,
      String name,
      String address,
      long area,
      int cost) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(
            InterfaceType.PHYSICAL)
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
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    OspfNetworkType
                        .POINT_TO_POINT)
                .build())
        .build();
  }

  private static Ospfv3Area area(
      long number,
      String... interfaces) {

    return Ospfv3Area.builder()
        .setNumber(number)
        .addInterfaces(
            List.of(interfaces))
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      Map<Long, Ospfv3Area> areas,
      Set<Ospfv3VirtualLink> virtualLinks) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areas)
        .setVirtualLinks(
            virtualLinks)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3InterAreaRoute6
      findInterArea(
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
        : (Ospfv3InterAreaRoute6)
            route;
  }

  @Test
  public void testMultiHopVirtualBackboneAndWithdrawal() {

    Node core =
        TestUtils.makeIosRouter(
            "core");

    Node abr1 =
        TestUtils.makeIosRouter(
            "abr1");

    Node transit =
        TestUtils.makeIosRouter(
            "transit");

    Node abr2 =
        TestUtils.makeIosRouter(
            "abr2");

    Node leaf =
        TestUtils.makeIosRouter(
            "leaf");

    /*
     * Area 0:
     *
     * core ---- abr1
     */
    addOspfInterface(
        core,
        "core-abr1",
        "2001:db8:10::1/64",
        0L,
        5);

    addOspfInterface(
        abr1,
        "abr1-core",
        "2001:db8:10::2/64",
        0L,
        10);

    /*
     * Transit area 1:
     *
     * abr1 ---- transit ---- abr2
     *
     * Directional virtual-link cost abr1 -> abr2:
     * 30 + 21 = 51.
     *
     * Directional virtual-link cost abr2 -> abr1:
     * 31 + 20 = 51.
     */
    addOspfInterface(
        abr1,
        "abr1-transit",
        "2001:db8:20::1/64",
        1L,
        30);

    addOspfInterface(
        transit,
        "transit-abr1",
        "2001:db8:20::2/64",
        1L,
        20);

    addOspfInterface(
        transit,
        "transit-abr2",
        "2001:db8:30::1/64",
        1L,
        21);

    addOspfInterface(
        abr2,
        "abr2-transit",
        "2001:db8:30::2/64",
        1L,
        31);

    /*
     * Area 2:
     *
     * abr2 ---- leaf
     */
    addOspfInterface(
        abr2,
        "abr2-leaf",
        "2001:db8:40::1/64",
        2L,
        15);

    addOspfInterface(
        leaf,
        "leaf-abr2",
        "2001:db8:40::2/64",
        2L,
        7);

    Ip abr1Id =
        Ip.parse(
            "192.0.2.11");

    Ip abr2Id =
        Ip.parse(
            "192.0.2.22");

    addProcess(
        core,
        "192.0.2.1",
        ImmutableMap.of(
            0L,
            area(
                0L,
                "core-abr1")),
        Set.of());

    addProcess(
        abr1,
        abr1Id.toString(),
        ImmutableMap.of(
            0L,
            area(
                0L,
                "abr1-core"),
            1L,
            area(
                1L,
                "abr1-transit")),
        Set.of(
            new Ospfv3VirtualLink(
                1L,
                abr2Id)));

    addProcess(
        transit,
        "192.0.2.100",
        ImmutableMap.of(
            1L,
            area(
                1L,
                "transit-abr1",
                "transit-abr2")),
        Set.of());

    addProcess(
        abr2,
        abr2Id.toString(),
        ImmutableMap.of(
            1L,
            area(
                1L,
                "abr2-transit"),
            2L,
            area(
                2L,
                "abr2-leaf")),
        Set.of(
            new Ospfv3VirtualLink(
                1L,
                abr1Id)));

    addProcess(
        leaf,
        "192.0.2.200",
        ImmutableMap.of(
            2L,
            area(
                2L,
                "leaf-abr2")),
        Set.of());

    VirtualRouter coreVr =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter abr1Vr =
        abr1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter transitVr =
        transit.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter abr2Vr =
        abr2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter leafVr =
        leaf.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    coreVr.initForIgpComputation(
        topology);

    abr1Vr.initForIgpComputation(
        topology);

    transitVr.initForIgpComputation(
        topology);

    abr2Vr.initForIgpComputation(
        topology);

    leafVr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "core", core,
            "abr1", abr1,
            "transit", transit,
            "abr2", abr2,
            "leaf", leaf);

    List<VirtualRouter> vrs =
        List.of(
            coreVr,
            abr1Vr,
            transitVr,
            abr2Vr,
            leafVr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-abr1"),
        NodeInterfacePair.of(
            "abr1",
            "abr1-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr1",
            "abr1-transit"),
        NodeInterfacePair.of(
            "transit",
            "transit-abr1"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "transit",
            "transit-abr2"),
        NodeInterfacePair.of(
            "abr2",
            "abr2-transit"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr2",
            "abr2-leaf"),
        NodeInterfacePair.of(
            "leaf",
            "leaf-abr2"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 backbonePrefix =
        Prefix6.parse(
            "2001:db8:10::/64");

    Prefix6 area2Prefix =
        Prefix6.parse(
            "2001:db8:40::/64");

    /*
     * abr1's area-0 route has metric 10.
     * abr2 reaches abr1 over the 51-cost virtual link.
     * leaf then adds its area-2 interface cost of 7.
     *
     * 10 + 51 + 7 = 68.
     */
    Ospfv3InterAreaRoute6 leafBackbone =
        findInterArea(
            leafVr,
            backbonePrefix);

    assertThat(
        leafBackbone,
        notNullValue());

    assertThat(
        leafBackbone.getMetric(),
        equalTo(68L));

    /*
     * abr2's local area-2 network has metric 15.
     * abr1 imports it across the 51-cost virtual link into area 0.
     * core then adds its area-0 interface cost of 5.
     *
     * 15 + 51 + 5 = 71.
     */
    Ospfv3InterAreaRoute6 coreArea2 =
        findInterArea(
            coreVr,
            area2Prefix);

    assertThat(
        coreArea2,
        notNullValue());

    assertThat(
        coreArea2.getMetric(),
        equalTo(71L));

    /*
     * Break the transit area. The reciprocal configuration remains, but
     * the virtual link must cease to be operational and both inter-area
     * routes must withdraw.
     */
    abr2.getConfiguration()
        .getAllInterfaces()
        .get("abr2-transit")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    abr2Vr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findInterArea(
            leafVr,
            backbonePrefix),
        nullValue());

    assertThat(
        findInterArea(
            coreVr,
            area2Prefix),
        nullValue());
  }
  @Test
  public void testVirtualLinkAuthenticationCompatibility() {

    org.batfish.datamodel.ospf.Ospfv3Authentication
        shared =
            new org.batfish.datamodel.ospf.Ospfv3Authentication(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .KeyType.PLAINTEXT,
                "shared-secret");

    org.batfish.datamodel.ospf.Ospfv3Authentication
        wrongKey =
            new org.batfish.datamodel.ospf.Ospfv3Authentication(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .KeyType.PLAINTEXT,
                "wrong-secret");

    Ospfv3VirtualLink lhs =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.2"),
            shared);

    Ospfv3VirtualLink rhsMatching =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.1"),
            shared);

    Ospfv3VirtualLink rhsWrongKey =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.1"),
            wrongKey);

    Ospfv3VirtualLink rhsNoAuth =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.1"));

    assertThat(
        Ospfv3RoutingProcess
            .areVirtualLinkAuthenticationsCompatible(
                lhs,
                rhsMatching),
        equalTo(
            true));

    assertThat(
        Ospfv3RoutingProcess
            .areVirtualLinkAuthenticationsCompatible(
                lhs,
                rhsWrongKey),
        equalTo(
            false));

    assertThat(
        Ospfv3RoutingProcess
            .areVirtualLinkAuthenticationsCompatible(
                lhs,
                rhsNoAuth),
        equalTo(
            false));

    assertThat(
        Ospfv3RoutingProcess
            .areVirtualLinkAuthenticationsCompatible(
                new Ospfv3VirtualLink(
                    1L,
                    Ip.parse(
                        "192.0.2.2")),
                rhsNoAuth),
        equalTo(
            true));
  }

}
