package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
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
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 distribute-list behavior. */
public final class Ospfv3DistributeListTest {

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
      InterfaceType type,
      int cost) {

    Configuration c =
        node.getConfiguration();

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
                .setAreaName(0L)
                .setCost(cost)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    type == InterfaceType.LOOPBACK
                        ? OspfNetworkType.BROADCAST
                        : OspfNetworkType.POINT_TO_POINT)
                .build())
        .build();
  }

  private static Interface addConnectedLoopback(
      Node node,
      String name,
      String address) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.LOOPBACK)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean redistributeConnected,
      PrefixList6 inbound,
      PrefixList6 outbound,
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
        .setInboundDistributeList(
            inbound)
        .setOutboundDistributeList(
            outbound)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static AbstractRoute6 findOspfRoute(
      VirtualRouter vr,
      Prefix6 prefix) {

    return vr.getOspfv3Processes()
        .get("1")
        .getRoutes()
        .stream()
        .filter(
            route ->
                route.getNetwork()
                    .equals(prefix))
        .findFirst()
        .orElse(null);
  }

  private static Ospfv3ExternalType2Route6
      findExternal(
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
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType2Route6)
            route;
  }

  @Test
  public void testInboundDoesNotFilterLsaAndOutboundFiltersRedistribution() {
    Node n1 =
        TestUtils.makeIosRouter("n1");

    Node n2 =
        TestUtils.makeIosRouter("n2");

    Node n3 =
        TestUtils.makeIosRouter("n3");

    addOspfInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL,
        10);

    addOspfInterface(
        n1,
        "loop100",
        "2001:db8:100::1/128",
        InterfaceType.LOOPBACK,
        1);

    addOspfInterface(
        n1,
        "loop200",
        "2001:db8:200::1/128",
        InterfaceType.LOOPBACK,
        1);

    addConnectedLoopback(
        n1,
        "external300",
        "2001:db8:300::1/128");

    addConnectedLoopback(
        n1,
        "external400",
        "2001:db8:400::1/128");

    addOspfInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL,
        20);

    addOspfInterface(
        n2,
        "eth23",
        "2001:db8:23::2/64",
        InterfaceType.PHYSICAL,
        30);

    addOspfInterface(
        n3,
        "eth32",
        "2001:db8:23::3/64",
        InterfaceType.PHYSICAL,
        40);

    PrefixList6 inbound =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.DENY,
                    Prefix6.parse(
                        "2001:db8:100::1/128"),
                    new SubRange(
                        128, 128)),
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.ZERO,
                    new SubRange(
                        0, 128))));

    PrefixList6 outbound =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.DENY,
                    Prefix6.parse(
                        "2001:db8:300::1/128"),
                    new SubRange(
                        128, 128)),
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.ZERO,
                    new SubRange(
                        0, 128))));

    addProcess(
        n1,
        "192.0.2.1",
        true,
        null,
        outbound,
        "eth12",
        "loop100",
        "loop200");

    addProcess(
        n2,
        "192.0.2.2",
        false,
        inbound,
        null,
        "eth21",
        "eth23");

    addProcess(
        n3,
        "192.0.2.3",
        false,
        null,
        null,
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
            ImmutableMap.of(
                "n1", n1,
                "n2", n2,
                "n3", n3),
            List.of(
                vr1, vr2, vr3),
            adjacencies);

    Prefix6 internalDenied =
        Prefix6.parse(
            "2001:db8:100::1/128");

    Prefix6 internalAllowed =
        Prefix6.parse(
            "2001:db8:200::1/128");

    Prefix6 externalDenied =
        Prefix6.parse(
            "2001:db8:300::1/128");

    Prefix6 externalAllowed =
        Prefix6.parse(
            "2001:db8:400::1/128");

    /*
     * n2 learns the denied route in OSPF control-plane state...
     */
    assertThat(
        findOspfRoute(
            vr2,
            internalDenied),
        notNullValue());

    /*
     * ...but the inbound distribute-list prevents installation.
     */
    assertThat(
        vr2.getMainRib6()
            .getRoutes(
                internalDenied),
        empty());

    /*
     * An allowed OSPF route is installed normally.
     */
    assertThat(
        vr2.getMainRib6()
            .getRoutes(
                internalAllowed),
        not(empty()));

    /*
     * The inbound distribute-list does NOT filter LSAs. n2 can still
     * propagate the denied route onward to n3, where n3 installs it.
     */
    assertThat(
        findOspfRoute(
            vr3,
            internalDenied),
        notNullValue());

    assertThat(
        vr3.getMainRib6()
            .getRoutes(
                internalDenied),
        not(empty()));

    /*
     * Outbound distribute-list suppresses the 300 prefix before
     * connected redistribution.
     */
    assertThat(
        findExternal(
            vr1,
            externalDenied),
        nullValue());

    assertThat(
        findExternal(
            vr2,
            externalDenied),
        nullValue());

    /*
     * The permitted redistributed prefix propagates normally.
     */
    assertThat(
        findExternal(
            vr2,
            externalAllowed),
        notNullValue());

    assertThat(
        findExternal(
            vr3,
            externalAllowed),
        notNullValue());

    assertThat(
        vr3.getMainRib6()
            .getRoutes(
                externalAllowed),
        not(empty()));
  }
}
