package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/**
 * End-to-end tests for permanent OSPFv3 max-metric router-lsa
 * stub-router behavior.
 */
public final class Ospfv3MaxMetricRouterLsaTest {

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

      _pairs.put(
          lhs,
          rhs);

      _pairs.put(
          rhs,
          lhs);
    }

    @Override
    public boolean inSameBroadcastDomain(
        NodeInterfacePair i1,
        NodeInterfacePair i2) {

      return Optional
          .ofNullable(
              _pairs.get(i1))
          .map(
              i2::equals)
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

  private static Interface addLink(
      Node node,
      String name,
      String address,
      int cost) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(
            c.getDefaultVrf())
        .setType(
            InterfaceType.PHYSICAL)
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
                .setNetworkType(
                    OspfNetworkType
                        .POINT_TO_POINT)
                .build())
        .build();
  }

  private static Interface addLoopback(
      Node node,
      String name,
      String address) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(
            c.getDefaultVrf())
        .setType(
            InterfaceType.LOOPBACK)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(0L)
                .setCost(1)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(true)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    OspfNetworkType.BROADCAST)
                .build())
        .build();
  }

  private static Ospfv3Area area(
      String... interfaces) {

    return Ospfv3Area.builder()
        .setNumber(0L)
        .addInterfaces(
            List.of(interfaces))
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean maxMetric,
      String... interfaces) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(
                0L,
                area(interfaces)))
        .setMaxMetricRouterLsa(
            maxMetric)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3IntraAreaRoute6 findRoute(
      VirtualRouter vr,
      Prefix6 prefix) {

    AbstractRoute6 route =
        vr.getOspfv3Processes()
            .get("1")
            .getRoutes()
            .stream()
            .filter(
                candidate ->
                    candidate
                        instanceof
                        Ospfv3IntraAreaRoute6)
            .filter(
                candidate ->
                    candidate
                        .getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3IntraAreaRoute6)
            route;
  }

  @Test
  public void testAvoidStubRouterAsTransit() {

    Node src =
        TestUtils.makeIosRouter(
            "src");

    Node stub =
        TestUtils.makeIosRouter(
            "stub");

    Node alt =
        TestUtils.makeIosRouter(
            "alt");

    Node dst =
        TestUtils.makeIosRouter(
            "dst");

    /*
     * Cheap physical path:
     *
     * src --1--> stub --1--> dst
     *
     * but stub advertises max-metric router-lsa.
     */
    addLink(
        src,
        "src-stub",
        "2001:db8:10::1/64",
        1);

    addLink(
        stub,
        "stub-src",
        "2001:db8:10::2/64",
        1);

    addLink(
        stub,
        "stub-dst",
        "2001:db8:20::1/64",
        1);

    addLink(
        dst,
        "dst-stub",
        "2001:db8:20::2/64",
        1);

    /*
     * More expensive ordinary path:
     *
     * src --10--> alt --10--> dst
     */
    addLink(
        src,
        "src-alt",
        "2001:db8:30::1/64",
        10);

    addLink(
        alt,
        "alt-src",
        "2001:db8:30::2/64",
        10);

    addLink(
        alt,
        "alt-dst",
        "2001:db8:40::1/64",
        10);

    addLink(
        dst,
        "dst-alt",
        "2001:db8:40::2/64",
        10);

    addLoopback(
        dst,
        "dst-loopback",
        "2001:db8:ffff::1/128");

    addLoopback(
        stub,
        "stub-loopback",
        "2001:db8:aaaa::1/128");

    addProcess(
        src,
        "192.0.2.1",
        false,
        "src-stub",
        "src-alt");

    addProcess(
        stub,
        "192.0.2.2",
        true,
        "stub-src",
        "stub-dst",
        "stub-loopback");

    addProcess(
        alt,
        "192.0.2.3",
        false,
        "alt-src",
        "alt-dst");

    addProcess(
        dst,
        "192.0.2.4",
        false,
        "dst-stub",
        "dst-alt",
        "dst-loopback");

    VirtualRouter srcVr =
        src.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter stubVr =
        stub.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter altVr =
        alt.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter dstVr =
        dst.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    srcVr.initForIgpComputation(
        topology);

    stubVr.initForIgpComputation(
        topology);

    altVr.initForIgpComputation(
        topology);

    dstVr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "src",
            src,
            "stub",
            stub,
            "alt",
            alt,
            "dst",
            dst);

    List<VirtualRouter> vrs =
        List.of(
            srcVr,
            stubVr,
            altVr,
            dstVr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "src",
            "src-stub"),
        NodeInterfacePair.of(
            "stub",
            "stub-src"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "stub",
            "stub-dst"),
        NodeInterfacePair.of(
            "dst",
            "dst-stub"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "src",
            "src-alt"),
        NodeInterfacePair.of(
            "alt",
            "alt-src"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "alt",
            "alt-dst"),
        NodeInterfacePair.of(
            "dst",
            "dst-alt"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:ffff::1/128");

    Ospfv3IntraAreaRoute6 route =
        findRoute(
            srcVr,
            destination);

    assertThat(
        route,
        notNullValue());

    /*
     * The ordinary alternate route wins:
     *
     * dst loopback = 1
     * alt -> dst = 10
     * src -> alt = 10
     *
     * total = 21
     */
    assertThat(
        route.getMetric(),
        equalTo(21L));

    assertThat(
        route.getNextHopInterface(),
        equalTo(
            "src-alt"));

    /*
     * Permanent max-metric must not make the stub router's own
     * directly-originated prefixes expensive.
     */
    Prefix6 stubLoopback =
        Prefix6.parse(
            "2001:db8:aaaa::1/128");

    Ospfv3IntraAreaRoute6 stubRoute =
        findRoute(
            srcVr,
            stubLoopback);

    assertThat(
        stubRoute,
        notNullValue());

    assertThat(
        stubRoute.getMetric(),
        equalTo(2L));

    assertThat(
        stubRoute.getNextHopInterface(),
        equalTo(
            "src-stub"));
  }
}
