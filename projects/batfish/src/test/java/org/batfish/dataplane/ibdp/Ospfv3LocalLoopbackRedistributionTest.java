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
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3ExternalSummary;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/**
 * End-to-end tests for AOS-CX OSPFv3 local-loopback redistribution.
 */
public final class Ospfv3LocalLoopbackRedistributionTest {

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

  private static Interface addLoopback(
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

  private static Interface addPhysicalSource(
      Node node,
      String name,
      String address) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .setAdminUp(true)
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

  private static RouteMap6.Entry entry(
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

  @Test
  public void testLocalLoopbackRouteMapAndWithdrawal() {

    Node node =
        TestUtils.makeIosRouter(
            "r1");

    Prefix6 localHost =
        Prefix6.parse(
            "2001:db8:100::1/128");

    Prefix6 rejectedHost =
        Prefix6.parse(
            "2001:db8:200::1/128");

    Prefix6 physicalHost =
        Prefix6.parse(
            "2001:db8:300::1/128");

    addLoopback(
        node,
        "loopback100",
        "2001:db8:100::1/64");

    addLoopback(
        node,
        "loopback200",
        "2001:db8:200::1/64");

    addPhysicalSource(
        node,
        "physical300",
        "2001:db8:300::1/64");

    RouteMap6 routeMap =
        new RouteMap6(
            Map.of(
                10L,
                entry(
                    localHost,
                    17L,
                    OspfMetricType.E1,
                    707L)));

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.1"))
        .setAreas(
            ImmutableMap.of(
                0L,
                area))
        .setRedistributeConnected(false)
        .setRedistributeLocalLoopback(true)
        .setRedistributeLocalLoopbackRouteMap(
            routeMap)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();

    VirtualRouter vr =
        node.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    vr.initForIgpComputation(
        topology);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "r1",
                node),
            List.of(vr),
            new NoAdjacencies());

    Ospfv3ExternalType1Route6 route =
        (Ospfv3ExternalType1Route6)
            findRoute(
                vr,
                localHost,
                Ospfv3ExternalType1Route6.class);

    assertThat(
        route,
        notNullValue());

    assertThat(
        route.getMetric(),
        equalTo(17L));

    assertThat(
        route.getLsaMetric(),
        equalTo(17L));

    assertThat(
        route.getTag(),
        equalTo(707L));

    /*
     * local loopback advertises the host-local /128 rather than the
     * configured loopback's connected /64.
     */
    assertThat(
        findRoute(
            vr,
            Prefix6.parse(
                "2001:db8:100::/64"),
            Ospfv3ExternalType1Route6.class),
        nullValue());

    /*
     * The route-map's implicit deny suppresses the second loopback.
     */
    assertThat(
        findRoute(
            vr,
            rejectedHost,
            Ospfv3ExternalType1Route6.class),
        nullValue());

    /*
     * A physical interface is not a "local loopback" redistribution
     * source.
     */
    assertThat(
        findRoute(
            vr,
            physicalHost,
            Ospfv3ExternalType1Route6.class),
        nullValue());

    node.getConfiguration()
        .getAllInterfaces()
        .get("loopback100")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vr.updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "r1",
                node),
            List.of(vr),
            new NoAdjacencies());

    assertThat(
        findRoute(
            vr,
            localHost,
            Ospfv3ExternalType1Route6.class),
        nullValue());
  }

  @Test
  public void testLocalLoopbackSummaryAndNssaType1() {

    Node normal =
        TestUtils.makeIosRouter(
            "normal");

    Prefix6 host1 =
        Prefix6.parse(
            "2001:db8:500:1::1/128");

    Prefix6 host2 =
        Prefix6.parse(
            "2001:db8:500:2::1/128");

    Prefix6 summary =
        Prefix6.parse(
            "2001:db8:500::/48");

    addLoopback(
        normal,
        "loopback1",
        "2001:db8:500:1::1/64");

    addLoopback(
        normal,
        "loopback2",
        "2001:db8:500:2::1/64");

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.10"))
        .setAreas(
            ImmutableMap.of(
                0L,
                Ospfv3Area.builder()
                    .setNumber(0L)
                    .build()))
        .setRedistributeLocalLoopback(true)
        .setExternalSummaries(
            Set.of(
                new Ospfv3ExternalSummary(
                    summary,
                    true,
                    5000L)))
        .setVrf(
            normal.getConfiguration()
                .getDefaultVrf())
        .build();

    VirtualRouter normalVr =
        normal.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    normalVr.initForIgpComputation(
        topology);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "normal",
                normal),
            List.of(normalVr),
            new NoAdjacencies());

    Ospfv3ExternalType2Route6 aggregate =
        (Ospfv3ExternalType2Route6)
            findRoute(
                normalVr,
                summary,
                Ospfv3ExternalType2Route6.class);

    assertThat(
        aggregate,
        notNullValue());

    assertThat(
        aggregate.getMetric(),
        equalTo(
            Ospfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));

    assertThat(
        aggregate.getTag(),
        equalTo(5000L));

    assertThat(
        findRoute(
            normalVr,
            host1,
            Ospfv3ExternalType2Route6.class),
        nullValue());

    assertThat(
        findRoute(
            normalVr,
            host2,
            Ospfv3ExternalType2Route6.class),
        nullValue());

    /*
     * Verify the same local-loopback source path produces N1 in an NSSA
     * when route-map metric type is E1.
     */
    Node nssa =
        TestUtils.makeIosRouter(
            "nssa");

    Prefix6 nssaHost =
        Prefix6.parse(
            "2001:db8:600::1/128");

    addLoopback(
        nssa,
        "loopback600",
        "2001:db8:600::1/64");

    RouteMap6 nssaMap =
        new RouteMap6(
            Map.of(
                10L,
                entry(
                    nssaHost,
                    31L,
                    OspfMetricType.E1,
                    606L)));

    Ospfv3Area nssaArea =
        Ospfv3Area.builder()
            .setNumber(1L)
            .setNssa(true)
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.20"))
        .setAreas(
            ImmutableMap.of(
                1L,
                nssaArea))
        .setRedistributeLocalLoopback(true)
        .setRedistributeLocalLoopbackRouteMap(
            nssaMap)
        .setVrf(
            nssa.getConfiguration()
                .getDefaultVrf())
        .build();

    VirtualRouter nssaVr =
        nssa.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    nssaVr.initForIgpComputation(
        topology);

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            ImmutableMap.of(
                "nssa",
                nssa),
            List.of(nssaVr),
            new NoAdjacencies());

    Ospfv3NssaExternalType1Route6 n1 =
        (Ospfv3NssaExternalType1Route6)
            findRoute(
                nssaVr,
                nssaHost,
                Ospfv3NssaExternalType1Route6.class);

    assertThat(
        n1,
        notNullValue());

    assertThat(
        n1.getMetric(),
        equalTo(31L));

    assertThat(
        n1.getLsaMetric(),
        equalTo(31L));

    assertThat(
        n1.getCostToAdvertiser(),
        equalTo(0L));

    assertThat(
        n1.getTag(),
        equalTo(606L));

    assertThat(
        n1.getArea(),
        equalTo(1L));
  }
}
