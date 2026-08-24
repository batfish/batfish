package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
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
import org.batfish.datamodel.ospf.Ospfv3ExternalSummary;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 ASBR summary-address behavior. */
public final class Ospfv3ExternalSummaryPropagationTest {

  private static final class NoAdjacencies
      implements L3Adjacencies {

    @Override
    public boolean inSameBroadcastDomain(
        NodeInterfacePair i1,
        NodeInterfacePair i2) {

      return false;
    }

    @Override
    public Optional<NodeInterfacePair>
        pairedPointToPointL3Interface(
            NodeInterfacePair iface) {

      return Optional.empty();
    }
  }

  private static Interface addOspfInterface(
      Node node,
      String name,
      String address,
      long area,
      boolean nssa) {

    Configuration c =
        node.getConfiguration();

    Interface iface =
        Interface.builder()
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
                    .setCost(10)
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

    return iface;
  }

  private static Interface addSource(
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

  private static RouteMap6.Entry routeMapEntry(
      Prefix6 prefix,
      long metric,
      OspfMetricType metricType,
      long tag) {

    return new RouteMap6.Entry(
        LineAction.PERMIT,
        exact(prefix),
        metric,
        metricType,
        tag);
  }

  private static void addProcess(
      Node node,
      String routerId,
      long areaNumber,
      boolean nssa,
      RouteMap6 routeMap,
      Set<Ospfv3ExternalSummary> summaries,
      String ospfInterface) {

    Ospfv3Area.Builder areaBuilder =
        Ospfv3Area.builder()
            .setNumber(areaNumber)
            .addInterface(
                ospfInterface);

    if (nssa) {
      areaBuilder.setNssa(true);
    }

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(
                areaNumber,
                areaBuilder.build()))
        .setRedistributeConnected(true)
        .setRedistributeConnectedRouteMap(
            routeMap)
        .setExternalSummaries(
            summaries)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static AbstractRoute6 findRoute(
      VirtualRouter vr,
      Prefix6 prefix,
      Class<? extends AbstractRoute6> type) {

    return vr.getOspfv3Processes()
        .get("1")
        .getRoutes()
        .stream()
        .filter(type::isInstance)
        .filter(
            route ->
                route.getNetwork()
                    .equals(prefix))
        .findFirst()
        .orElse(null);
  }

  private static Ospfv3ExternalType1Route6 findE1(
      VirtualRouter vr,
      Prefix6 prefix) {

    return (Ospfv3ExternalType1Route6)
        findRoute(
            vr,
            prefix,
            Ospfv3ExternalType1Route6.class);
  }

  private static Ospfv3ExternalType2Route6 findE2(
      VirtualRouter vr,
      Prefix6 prefix) {

    return (Ospfv3ExternalType2Route6)
        findRoute(
            vr,
            prefix,
            Ospfv3ExternalType2Route6.class);
  }

  private static Ospfv3NssaExternalType1Route6
      findN1(
          VirtualRouter vr,
          Prefix6 prefix) {

    return (Ospfv3NssaExternalType1Route6)
        findRoute(
            vr,
            prefix,
            Ospfv3NssaExternalType1Route6.class);
  }

  private static Ospfv3NssaExternalType2Route6
      findN2(
          VirtualRouter vr,
          Prefix6 prefix) {

    return (Ospfv3NssaExternalType2Route6)
        findRoute(
            vr,
            prefix,
            Ospfv3NssaExternalType2Route6.class);
  }

  @Test
  public void testType5SummariesNoAdvertiseTagAndWithdrawal() {

    Node asbr =
        TestUtils.makeIosRouter(
            "asbr");

    addOspfInterface(
        asbr,
        "ospf0",
        "2001:db8::1/64",
        0L,
        false);

    Prefix6 p100a =
        Prefix6.parse(
            "2001:db8:100:1::1/128");

    Prefix6 p100b =
        Prefix6.parse(
            "2001:db8:100:2::1/128");

    Prefix6 p200a =
        Prefix6.parse(
            "2001:db8:200:1::1/128");

    Prefix6 p200b =
        Prefix6.parse(
            "2001:db8:200:2::1/128");

    Prefix6 p300 =
        Prefix6.parse(
            "2001:db8:300:1::1/128");

    Prefix6 p400 =
        Prefix6.parse(
            "2001:db8:400:1::1/128");

    Prefix6 summary100 =
        Prefix6.parse(
            "2001:db8:100::/48");

    Prefix6 summary200 =
        Prefix6.parse(
            "2001:db8:200::/48");

    Prefix6 summary300 =
        Prefix6.parse(
            "2001:db8:300::/48");

    addSource(
        asbr,
        "s100a",
        p100a.toString());

    addSource(
        asbr,
        "s100b",
        p100b.toString());

    addSource(
        asbr,
        "s200a",
        p200a.toString());

    addSource(
        asbr,
        "s200b",
        p200b.toString());

    addSource(
        asbr,
        "s300",
        p300.toString());

    addSource(
        asbr,
        "s400",
        p400.toString());

    RouteMap6 routeMap =
        new RouteMap6(
            Map.of(
                10L,
                routeMapEntry(
                    p100a,
                    30L,
                    OspfMetricType.E2,
                    101L),
                20L,
                routeMapEntry(
                    p100b,
                    20L,
                    OspfMetricType.E2,
                    102L),
                30L,
                routeMapEntry(
                    p200a,
                    40L,
                    OspfMetricType.E1,
                    201L),
                40L,
                routeMapEntry(
                    p200b,
                    10L,
                    OspfMetricType.E2,
                    202L),
                50L,
                routeMapEntry(
                    p300,
                    5L,
                    OspfMetricType.E2,
                    301L),
                60L,
                routeMapEntry(
                    p400,
                    7L,
                    OspfMetricType.E2,
                    401L)));

    addProcess(
        asbr,
        "192.0.2.1",
        0L,
        false,
        routeMap,
        Set.of(
            new Ospfv3ExternalSummary(
                summary100,
                true,
                1000L),
            new Ospfv3ExternalSummary(
                summary200,
                true,
                null),
            new Ospfv3ExternalSummary(
                summary300,
                false,
                null)),
        "ospf0");

    VirtualRouter vr =
        asbr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    vr.initForIgpComputation(
        topology);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "asbr",
                asbr),
            List.of(vr),
            new NoAdjacencies());

    /*
     * All-E2 contributors produce an E2 summary. Aruba uses the
     * lowest component metric: min(30,20)=20.
     */
    Ospfv3ExternalType2Route6 e2Summary =
        findE2(
            vr,
            summary100);

    assertThat(
        e2Summary,
        notNullValue());

    assertThat(
        e2Summary.getMetric(),
        equalTo(20L));

    assertThat(
        e2Summary.getTag(),
        equalTo(1000L));

    /*
     * If any component is E1, the aggregate is E1. Its initial metric
     * is still the lowest component metric: min(40,10)=10.
     */
    Ospfv3ExternalType1Route6 e1Summary =
        findE1(
            vr,
            summary200);

    assertThat(
        e1Summary,
        notNullValue());

    assertThat(
        e1Summary.getMetric(),
        equalTo(10L));

    assertThat(
        e1Summary.getLsaMetric(),
        equalTo(10L));

    assertThat(
        e1Summary.getTag(),
        equalTo(
            Route.UNSET_ROUTE_TAG));

    /*
     * Summary-address suppresses contributing specifics.
     */
    assertThat(
        findE2(
            vr,
            p100a),
        nullValue());

    assertThat(
        findE2(
            vr,
            p100b),
        nullValue());

    assertThat(
        findE1(
            vr,
            p200a),
        nullValue());

    assertThat(
        findE2(
            vr,
            p200b),
        nullValue());

    /*
     * no-advertise suppresses both aggregate and specific.
     */
    assertThat(
        findE2(
            vr,
            summary300),
        nullValue());

    assertThat(
        findE2(
            vr,
            p300),
        nullValue());

    /*
     * An unmatched redistributed route remains specific and retains
     * route-map metric and tag.
     */
    Ospfv3ExternalType2Route6 specific =
        findE2(
            vr,
            p400);

    assertThat(
        specific,
        notNullValue());

    assertThat(
        specific.getMetric(),
        equalTo(7L));

    assertThat(
        specific.getTag(),
        equalTo(401L));

    /*
     * Remove every contributor to 100/48. The aggregate must withdraw.
     */
    asbr.getConfiguration()
        .getAllInterfaces()
        .get("s100a")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    asbr.getConfiguration()
        .getAllInterfaces()
        .get("s100b")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vr.updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "asbr",
                asbr),
            List.of(vr),
            new NoAdjacencies());

    assertThat(
        findE2(
            vr,
            summary100),
        nullValue());
  }

  @Test
  public void testNssaSummariesProduceN1AndN2() {

    Node asbr =
        TestUtils.makeIosRouter(
            "nssa-asbr");

    addOspfInterface(
        asbr,
        "ospf1",
        "2001:db8:1::1/64",
        1L,
        true);

    Prefix6 p500a =
        Prefix6.parse(
            "2001:db8:500:1::1/128");

    Prefix6 p500b =
        Prefix6.parse(
            "2001:db8:500:2::1/128");

    Prefix6 p600 =
        Prefix6.parse(
            "2001:db8:600:1::1/128");

    Prefix6 summary500 =
        Prefix6.parse(
            "2001:db8:500::/48");

    Prefix6 summary600 =
        Prefix6.parse(
            "2001:db8:600::/48");

    addSource(
        asbr,
        "s500a",
        p500a.toString());

    addSource(
        asbr,
        "s500b",
        p500b.toString());

    addSource(
        asbr,
        "s600",
        p600.toString());

    RouteMap6 routeMap =
        new RouteMap6(
            Map.of(
                10L,
                routeMapEntry(
                    p500a,
                    30L,
                    OspfMetricType.E1,
                    501L),
                20L,
                routeMapEntry(
                    p500b,
                    10L,
                    OspfMetricType.E2,
                    502L),
                30L,
                routeMapEntry(
                    p600,
                    12L,
                    OspfMetricType.E2,
                    601L)));

    addProcess(
        asbr,
        "192.0.2.50",
        1L,
        true,
        routeMap,
        Set.of(
            new Ospfv3ExternalSummary(
                summary500,
                true,
                5000L),
            new Ospfv3ExternalSummary(
                summary600,
                true,
                null)),
        "ospf1");

    VirtualRouter vr =
        asbr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    vr.initForIgpComputation(
        topology);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "nssa-asbr",
                asbr),
            List.of(vr),
            new NoAdjacencies());

    Ospfv3NssaExternalType1Route6 n1 =
        findN1(
            vr,
            summary500);

    assertThat(
        n1,
        notNullValue());

    assertThat(
        n1.getMetric(),
        equalTo(10L));

    assertThat(
        n1.getLsaMetric(),
        equalTo(10L));

    assertThat(
        n1.getCostToAdvertiser(),
        equalTo(0L));

    assertThat(
        n1.getTag(),
        equalTo(5000L));

    Ospfv3NssaExternalType2Route6 n2 =
        findN2(
            vr,
            summary600);

    assertThat(
        n2,
        notNullValue());

    assertThat(
        n2.getMetric(),
        equalTo(12L));

    assertThat(
        n2.getCostToAdvertiser(),
        equalTo(0L));

    assertThat(
        n2.getTag(),
        equalTo(
            Route.UNSET_ROUTE_TAG));

    assertThat(
        findN1(
            vr,
            p500a),
        nullValue());

    assertThat(
        findN2(
            vr,
            p500b),
        nullValue());

    assertThat(
        findN2(
            vr,
            p600),
        nullValue());
  }
}
