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
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 NSSA Type-7 range aggregation. */
public final class Ospfv3NssaRangePropagationTest {

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
        .setType(InterfaceType.PHYSICAL)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .setBandwidth(
            10_000_000_000D)
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(area)
                .setCost(cost)
                .setDeadInterval(40)
                .setEnabled(true)
                .setHelloInterval(10)
                .setNetworkType(
                    OspfNetworkType.POINT_TO_POINT)
                .setPassive(false)
                .setPriority(1)
                .setProcess("1")
                .setRetransmitInterval(5)
                .setTransitDelay(1)
                .build())
        .build();
  }

  private static Interface addExternal(
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

  private static Ospfv3Area normalArea(
      long number,
      String... interfaces) {

    return Ospfv3Area.builder()
        .setNumber(number)
        .addInterfaces(
            List.of(interfaces))
        .build();
  }

  private static Ospfv3Area nssaArea(
      long number,
      List<Ospfv3AreaRange> ranges,
      String... interfaces) {

    Ospfv3Area.Builder builder =
        Ospfv3Area.builder()
            .setNumber(number)
            .setNssa(true)
            .addInterfaces(
                List.of(interfaces));

    ranges.forEach(
        builder::addRange);

    return builder.build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean redistributeConnected,
      long redistributionMetric,
      Map<Long, Ospfv3Area> areas) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areas)
        .setRedistributeConnected(
            redistributeConnected)
        .setRedistributionMetric(
            redistributionMetric)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3NssaExternalType2Route6
      findType7(
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
                        Ospfv3NssaExternalType2Route6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3NssaExternalType2Route6)
            route;
  }

  private static Ospfv3ExternalType2Route6
      findType5(
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
  public void testAggregationNoAdvertiseAndWithdrawal() {

    Node core =
        TestUtils.makeIosRouter("core");

    Node abr =
        TestUtils.makeIosRouter("abr");

    Node asbr1 =
        TestUtils.makeIosRouter("asbr1");

    Node asbr2 =
        TestUtils.makeIosRouter("asbr2");

    addOspfInterface(
        core,
        "core-abr",
        "2001:db8:0::1/64",
        0L,
        5);

    addOspfInterface(
        abr,
        "abr-core",
        "2001:db8:0::2/64",
        0L,
        10);

    addOspfInterface(
        abr,
        "abr-asbr1",
        "2001:db8:1::1/64",
        1L,
        30);

    addOspfInterface(
        asbr1,
        "asbr1-abr",
        "2001:db8:1::2/64",
        1L,
        40);

    addOspfInterface(
        abr,
        "abr-asbr2",
        "2001:db8:2::1/64",
        1L,
        31);

    addOspfInterface(
        asbr2,
        "asbr2-abr",
        "2001:db8:2::2/64",
        1L,
        41);

    addExternal(
        asbr1,
        "ext700a",
        "2001:db8:700:1::1/64");

    addExternal(
        asbr2,
        "ext700b",
        "2001:db8:700:2::1/64");

    addExternal(
        asbr1,
        "ext800",
        "2001:db8:800:1::1/64");

    addExternal(
        asbr1,
        "ext900",
        "2001:db8:900::1/48");

    addProcess(
        core,
        "192.0.2.1",
        false,
        25L,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-abr")));

    Ospfv3Area abrNssa =
        nssaArea(
            1L,
            List.of(
                new Ospfv3AreaRange(
                    Prefix6.parse(
                        "2001:db8:700::/48"),
                    Ospfv3AreaRange.Type.NSSA,
                    true),
                new Ospfv3AreaRange(
                    Prefix6.parse(
                        "2001:db8:800::/48"),
                    Ospfv3AreaRange.Type.NSSA,
                    false),
                new Ospfv3AreaRange(
                    Prefix6.parse(
                        "2001:db8:900::/48"),
                    Ospfv3AreaRange.Type.NSSA,
                    true)),
            "abr-asbr1",
            "abr-asbr2");

    addProcess(
        abr,
        "192.0.2.10",
        false,
        25L,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "abr-core"),
            1L,
            abrNssa));

    addProcess(
        asbr1,
        "192.0.2.11",
        true,
        10L,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                List.of(),
                "asbr1-abr")));

    addProcess(
        asbr2,
        "192.0.2.12",
        true,
        20L,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                List.of(),
                "asbr2-abr")));

    VirtualRouter coreVr =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter abrVr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbr1Vr =
        asbr1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbr2Vr =
        asbr2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    coreVr.initForIgpComputation(topology);
    abrVr.initForIgpComputation(topology);
    asbr1Vr.initForIgpComputation(topology);
    asbr2Vr.initForIgpComputation(topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "core", core,
            "abr", abr,
            "asbr1", asbr1,
            "asbr2", asbr2);

    List<VirtualRouter> vrs =
        List.of(
            coreVr,
            abrVr,
            asbr1Vr,
            asbr2Vr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-abr"),
        NodeInterfacePair.of(
            "abr",
            "abr-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr",
            "abr-asbr1"),
        NodeInterfacePair.of(
            "asbr1",
            "asbr1-abr"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr",
            "abr-asbr2"),
        NodeInterfacePair.of(
            "asbr2",
            "asbr2-abr"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 componentA =
        Prefix6.parse(
            "2001:db8:700:1::/64");

    Prefix6 componentB =
        Prefix6.parse(
            "2001:db8:700:2::/64");

    Prefix6 summary =
        Prefix6.parse(
            "2001:db8:700::/48");

    Prefix6 hidden =
        Prefix6.parse(
            "2001:db8:800:1::/64");

    Prefix6 hiddenSummary =
        Prefix6.parse(
            "2001:db8:800::/48");

    Prefix6 exact =
        Prefix6.parse(
            "2001:db8:900::/48");

    assertThat(
        findType7(
            abrVr,
            componentA),
        notNullValue());

    assertThat(
        findType7(
            abrVr,
            componentB),
        notNullValue());

    assertThat(
        findType7(
            abrVr,
            hidden),
        notNullValue());

    Ospfv3ExternalType2Route6 aggregated =
        findType5(
            coreVr,
            summary);

    assertThat(
        aggregated,
        notNullValue());

    assertThat(
        aggregated.getMetric(),
        equalTo(21L));

    assertThat(
        findType5(
            coreVr,
            componentA),
        nullValue());

    assertThat(
        findType5(
            coreVr,
            componentB),
        nullValue());

    assertThat(
        findType5(
            coreVr,
            hidden),
        nullValue());

    assertThat(
        findType5(
            coreVr,
            hiddenSummary),
        nullValue());

    Ospfv3ExternalType2Route6 exactTranslation =
        findType5(
            coreVr,
            exact);

    assertThat(
        exactTranslation,
        notNullValue());

    assertThat(
        exactTranslation.getMetric(),
        equalTo(10L));

    asbr2.getConfiguration()
        .getAllInterfaces()
        .get("ext700b")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbr2Vr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    aggregated =
        findType5(
            coreVr,
            summary);

    assertThat(
        aggregated,
        notNullValue());

    assertThat(
        aggregated.getMetric(),
        equalTo(11L));

    asbr1.getConfiguration()
        .getAllInterfaces()
        .get("ext700a")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbr1Vr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findType5(
            coreVr,
            summary),
        nullValue());
  }
}
