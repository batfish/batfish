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
import javax.annotation.Nullable;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Ospfv3ExternalType1Route6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3NssaExternalType1Route6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 NSSA N1 behavior. */
public final class Ospfv3NssaType1PropagationTest {

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
        .setType(
            InterfaceType.LOOPBACK)
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
      @Nullable RouteMap6
          redistributeConnectedRouteMap,
      Map<Long, Ospfv3Area> areas) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areas)
        .setRedistributeConnected(
            redistributeConnectedRouteMap
                != null)
        .setRedistributeConnectedRouteMap(
            redistributeConnectedRouteMap)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static PrefixList6 exact(
      Prefix6 prefix) {

    int length =
        prefix.getPrefixLength();

    return new PrefixList6(
        List.of(
            new PrefixList6.Line(
                LineAction.PERMIT,
                prefix,
                new SubRange(
                    length,
                    length))));
  }

  private static Ospfv3NssaExternalType1Route6
      findN1(
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
                        Ospfv3NssaExternalType1Route6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3NssaExternalType1Route6)
            route;
  }

  private static Ospfv3NssaExternalType2Route6
      findN2(
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

  private static Ospfv3ExternalType1Route6
      findE1(
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
                        Ospfv3ExternalType1Route6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType1Route6)
            route;
  }

  private static Ospfv3ExternalType2Route6
      findE2(
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
  public void testN1PropagationTranslationAndWithdrawal() {

    Node core =
        TestUtils.makeIosRouter(
            "core");

    Node abr =
        TestUtils.makeIosRouter(
            "abr");

    Node asbr =
        TestUtils.makeIosRouter(
            "asbr");

    addOspfInterface(
        core,
        "core-abr",
        "2001:db8:10::1/64",
        0L,
        10);

    addOspfInterface(
        abr,
        "abr-core",
        "2001:db8:10::2/64",
        0L,
        20);

    addOspfInterface(
        abr,
        "abr-asbr",
        "2001:db8:20::1/64",
        1L,
        30);

    addOspfInterface(
        asbr,
        "asbr-abr",
        "2001:db8:20::2/64",
        1L,
        40);

    addExternal(
        asbr,
        "external-e1",
        "2001:db8:beef::1/128");

    RouteMap6 e1RouteMap =
        new RouteMap6(
            Map.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    25L,
                    OspfMetricType.E1,
                    501L)));

    addProcess(
        core,
        "192.0.2.1",
        null,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-abr")));

    addProcess(
        abr,
        "192.0.2.2",
        null,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "abr-core"),
            1L,
            nssaArea(
                1L,
                List.of(),
                "abr-asbr")));

    addProcess(
        asbr,
        "192.0.2.3",
        e1RouteMap,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                List.of(),
                "asbr-abr")));

    VirtualRouter coreVr =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter abrVr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbrVr =
        asbr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    coreVr.initForIgpComputation(
        topology);

    abrVr.initForIgpComputation(
        topology);

    asbrVr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "core", core,
            "abr", abr,
            "asbr", asbr);

    List<VirtualRouter> vrs =
        List.of(
            coreVr,
            abrVr,
            asbrVr);

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
            "abr-asbr"),
        NodeInterfacePair.of(
            "asbr",
            "asbr-abr"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:beef::1/128");

    Ospfv3NssaExternalType1Route6 local =
        findN1(
            asbrVr,
            prefix);

    assertThat(
        local,
        notNullValue());

    assertThat(
        local.getMetric(),
        equalTo(25L));

    assertThat(
        local.getLsaMetric(),
        equalTo(25L));

    assertThat(
        local.getCostToAdvertiser(),
        equalTo(0L));

    assertThat(
        local.getArea(),
        equalTo(1L));

    assertThat(
        local.getTag(),
        equalTo(501L));

    Ospfv3NssaExternalType1Route6 learned =
        findN1(
            abrVr,
            prefix);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getMetric(),
        equalTo(55L));

    assertThat(
        learned.getLsaMetric(),
        equalTo(25L));

    assertThat(
        learned.getCostToAdvertiser(),
        equalTo(30L));

    /*
     * The ABR translates N1 to E1. Since this VI model does not yet
     * represent the RFC forwarding-address field, the translated route
     * retains the 30 cost already accumulated inside the NSSA. The core
     * then adds its receiving-interface cost 10:
     *
     * 25 external + 30 NSSA + 10 backbone = 65.
     */
    Ospfv3ExternalType1Route6 translated =
        findE1(
            coreVr,
            prefix);

    assertThat(
        translated,
        notNullValue());

    assertThat(
        translated.getMetric(),
        equalTo(65L));

    assertThat(
        translated.getLsaMetric(),
        equalTo(25L));

    assertThat(
        translated.getCostToAdvertiser(),
        equalTo(40L));

    assertThat(
        translated.getAdvertiser(),
        equalTo(
            Ip.parse(
                "192.0.2.2")));

    assertThat(
        findN1(
            coreVr,
            prefix),
        nullValue());

    /*
     * Remove the redistributed source and reconverge. Both N1 and
     * translated E1 must withdraw.
     */
    asbr.getConfiguration()
        .getAllInterfaces()
        .get("external-e1")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbrVr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findN1(
            asbrVr,
            prefix),
        nullValue());

    assertThat(
        findN1(
            abrVr,
            prefix),
        nullValue());

    assertThat(
        findE1(
            coreVr,
            prefix),
        nullValue());
  }

  @Test
  public void testN1AndMixedNssaRangeAggregation() {

    Node core =
        TestUtils.makeIosRouter(
            "core");

    Node abr =
        TestUtils.makeIosRouter(
            "abr");

    Node asbr1 =
        TestUtils.makeIosRouter(
            "asbr1");

    Node asbr2 =
        TestUtils.makeIosRouter(
            "asbr2");

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

    Prefix6 p700a =
        Prefix6.parse(
            "2001:db8:700:1::/64");

    Prefix6 p700b =
        Prefix6.parse(
            "2001:db8:700:2::/64");

    Prefix6 p800a =
        Prefix6.parse(
            "2001:db8:800:1::/64");

    Prefix6 p800b =
        Prefix6.parse(
            "2001:db8:800:2::/64");

    Prefix6 summary700 =
        Prefix6.parse(
            "2001:db8:700::/48");

    Prefix6 summary800 =
        Prefix6.parse(
            "2001:db8:800::/48");

    addExternal(
        asbr1,
        "ext700a",
        "2001:db8:700:1::1/64");

    addExternal(
        asbr1,
        "ext800a",
        "2001:db8:800:1::1/64");

    addExternal(
        asbr2,
        "ext700b",
        "2001:db8:700:2::1/64");

    addExternal(
        asbr2,
        "ext800b",
        "2001:db8:800:2::1/64");

    RouteMap6 asbr1RouteMap =
        new RouteMap6(
            Map.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    exact(p700a),
                    10L,
                    OspfMetricType.E1,
                    701L),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    exact(p800a),
                    15L,
                    OspfMetricType.E1,
                    801L)));

    RouteMap6 asbr2RouteMap =
        new RouteMap6(
            Map.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    exact(p700b),
                    20L,
                    OspfMetricType.E1,
                    702L),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    exact(p800b),
                    30L,
                    OspfMetricType.E2,
                    802L)));

    addProcess(
        core,
        "192.0.2.1",
        null,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-abr")));

    addProcess(
        abr,
        "192.0.2.10",
        null,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "abr-core"),
            1L,
            nssaArea(
                1L,
                List.of(
                    new Ospfv3AreaRange(
                        summary700,
                        Ospfv3AreaRange.Type.NSSA,
                        true),
                    new Ospfv3AreaRange(
                        summary800,
                        Ospfv3AreaRange.Type.NSSA,
                        true)),
                "abr-asbr1",
                "abr-asbr2")));

    addProcess(
        asbr1,
        "192.0.2.11",
        asbr1RouteMap,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                List.of(),
                "asbr1-abr")));

    addProcess(
        asbr2,
        "192.0.2.12",
        asbr2RouteMap,
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

    coreVr.initForIgpComputation(
        topology);

    abrVr.initForIgpComputation(
        topology);

    asbr1Vr.initForIgpComputation(
        topology);

    asbr2Vr.initForIgpComputation(
        topology);

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

    /*
     * N1 contributors accumulate NSSA internal cost.
     */
    assertThat(
        findN1(
            abrVr,
            p700a)
            .getMetric(),
        equalTo(40L));

    assertThat(
        findN1(
            abrVr,
            p700b)
            .getMetric(),
        equalTo(51L));

    assertThat(
        findN1(
            abrVr,
            p800a)
            .getMetric(),
        equalTo(45L));

    assertThat(
        findN2(
            abrVr,
            p800b)
            .getMetric(),
        equalTo(30L));

    /*
     * 700/48 contains only N1 routes. RFC 3101 selects Type-1 and the
     * highest Type-1 cost at the translating ABR: max(40, 51) = 51.
     * The core then adds its receiving cost 5, producing total E1 cost 56.
     */
    Ospfv3ExternalType1Route6 n1Summary =
        findE1(
            coreVr,
            summary700);

    assertThat(
        n1Summary,
        notNullValue());

    assertThat(
        n1Summary.getMetric(),
        equalTo(56L));

    assertThat(
        n1Summary.getLsaMetric(),
        equalTo(51L));

    assertThat(
        n1Summary.getCostToAdvertiser(),
        equalTo(5L));

    assertThat(
        n1Summary.getTag(),
        equalTo(
            Route.UNSET_ROUTE_TAG));

    /*
     * 800/48 has one N1 and one N2. Any Type-2 contributor makes the
     * aggregate Type-2, with highest Type-2 metric + 1 = 31.
     */
    Ospfv3ExternalType2Route6 mixedSummary =
        findE2(
            coreVr,
            summary800);

    assertThat(
        mixedSummary,
        notNullValue());

    assertThat(
        mixedSummary.getMetric(),
        equalTo(31L));

    assertThat(
        mixedSummary.getCostToAdvertiser(),
        equalTo(5L));

    assertThat(
        mixedSummary.getTag(),
        equalTo(
            Route.UNSET_ROUTE_TAG));

    /*
     * Range translation suppresses component specifics outside the NSSA.
     */
    assertThat(
        findE1(
            coreVr,
            p700a),
        nullValue());

    assertThat(
        findE1(
            coreVr,
            p700b),
        nullValue());

    assertThat(
        findE1(
            coreVr,
            p800a),
        nullValue());

    assertThat(
        findE2(
            coreVr,
            p800b),
        nullValue());

    /*
     * Withdraw both ASBR2 contributors. 700/48 must recalculate to the
     * surviving N1 cost, while 800/48 must change path type from E2 to E1.
     */
    asbr2.getConfiguration()
        .getAllInterfaces()
        .get("ext700b")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbr2.getConfiguration()
        .getAllInterfaces()
        .get("ext800b")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbr2Vr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    n1Summary =
        findE1(
            coreVr,
            summary700);

    assertThat(
        n1Summary,
        notNullValue());

    /*
     * Surviving N1: external 10 + ABR NSSA cost 30 = 40;
     * core adds 5 -> 45.
     */
    assertThat(
        n1Summary.getMetric(),
        equalTo(45L));

    assertThat(
        n1Summary.getLsaMetric(),
        equalTo(40L));

    assertThat(
        findE2(
            coreVr,
            summary800),
        nullValue());

    Ospfv3ExternalType1Route6
        changedTypeSummary =
            findE1(
                coreVr,
                summary800);

    assertThat(
        changedTypeSummary,
        notNullValue());

    /*
     * Surviving N1: external 15 + ABR NSSA cost 30 = 45;
     * core adds 5 -> 50.
     */
    assertThat(
        changedTypeSummary.getMetric(),
        equalTo(50L));

    assertThat(
        changedTypeSummary.getLsaMetric(),
        equalTo(45L));
  }
}
