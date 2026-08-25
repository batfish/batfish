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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Ospfv3ExternalType1Route6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/**
 * End-to-end tests for redistribution between local OSPFv3 processes.
 */
public final class Ospfv3ProcessRedistributionTest {

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

  private static Interface addOspfInterface(
      Node node,
      String name,
      String address,
      String processId,
      boolean passive,
      InterfaceType type,
      int cost) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(
            c.getDefaultVrf())
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
                .setProcess(
                    processId)
                .setEnabled(true)
                .setPassive(
                    passive)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    type == InterfaceType.LOOPBACK
                        ? OspfNetworkType.BROADCAST
                        : OspfNetworkType
                            .POINT_TO_POINT)
                .build())
        .build();
  }

  private static Ospfv3Area area(
      String interfaceName) {

    return Ospfv3Area.builder()
        .setNumber(0L)
        .addInterface(
            interfaceName)
        .build();
  }

  private static void addProcess(
      Node node,
      String processId,
      String routerId,
      String interfaceName,
      boolean redistributeStatic,
      RouteMap6 redistributeStaticRouteMap,
      Set<String> redistributeOspfProcesses,
      Map<String, RouteMap6>
          redistributeOspfRouteMaps) {

    Ospfv3Process.builder()
        .setProcessId(
            processId)
        .setRouterId(
            Ip.parse(
                routerId))
        .setAreas(
            ImmutableMap.of(
                0L,
                area(
                    interfaceName)))
        .setRedistributeStatic(
            redistributeStatic)
        .setRedistributeStaticRouteMap(
            redistributeStaticRouteMap)
        .setRedistributeOspfProcesses(
            redistributeOspfProcesses)
        .setRedistributeOspfRouteMaps(
            redistributeOspfRouteMaps)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3ExternalType1Route6
      findType1(
          Ospfv3RoutingProcess process,
          Prefix6 prefix) {

    AbstractRoute6 route =
        process
            .getRoutes()
            .stream()
            .filter(
                candidate ->
                    candidate
                        instanceof
                        Ospfv3ExternalType1Route6)
            .filter(
                candidate ->
                    candidate
                        .getNetwork()
                        .equals(
                            prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType1Route6)
            route;
  }

  private static Ospfv3ExternalType2Route6
      findType2(
          Ospfv3RoutingProcess process,
          Prefix6 prefix) {

    AbstractRoute6 route =
        process
            .getRoutes()
            .stream()
            .filter(
                candidate ->
                    candidate
                        instanceof
                        Ospfv3ExternalType2Route6)
            .filter(
                candidate ->
                    candidate
                        .getNetwork()
                        .equals(
                            prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType2Route6)
            route;
  }

  @Test
  public void
      testRedistributeOspfRouteMapReciprocalGuardAndWithdrawal() {

    Node r1 =
        TestUtils.makeIosRouter(
            "r1");

    Node r2 =
        TestUtils.makeIosRouter(
            "r2");

    /*
     * Process 1 on r1 forms the real OSPF adjacency to r2.
     */
    addOspfInterface(
        r1,
        "r1-r2",
        "2001:db8:12::1/64",
        "1",
        false,
        InterfaceType.PHYSICAL,
        10);

    addOspfInterface(
        r2,
        "r2-r1",
        "2001:db8:12::2/64",
        "1",
        false,
        InterfaceType.PHYSICAL,
        20);

    /*
     * Process 2 exists only on r1 and has its own area-0 loopback.
     */
    addOspfInterface(
        r1,
        "process2-loopback",
        "2001:db8:2::1/128",
        "2",
        true,
        InterfaceType.LOOPBACK,
        1);

    Prefix6 redistributedPrefix =
        Prefix6.parse(
            "2001:db8:200::/64");

    StaticRoute6 sourceStatic =
        StaticRoute6.builder()
            .setNetwork(
                redistributedPrefix)
            .setNextHopInterface(
                Interface.NULL_INTERFACE_NAME)
            .setTag(
                222L)
            .build();

    r1.getConfiguration()
        .getDefaultVrf()
        .getStaticRoutes6()
        .add(
            sourceStatic);

    /*
     * Process 2 imports the tagged static route as its own E2 external.
     *
     * Process 1 redistributes process 2, but only tag 222 is accepted.
     * It changes that route to E1 metric 40/tag 999.
     */
    RouteMap6 process2StaticRouteMap =
        new RouteMap6(
            Map.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.STATIC,
                    null,
                    null,
                    null)));

    RouteMap6 process1RouteMap =
        new RouteMap6(
            Map.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    222L,
                    RoutingProtocol.OSPF3,
                    40L,
                    OspfMetricType.E1,
                    999L)));

    addProcess(
        r1,
        "1",
        "192.0.2.1",
        "r1-r2",
        false,
        null,
        Set.of(
            "2"),
        Map.of(
            "2",
            process1RouteMap));

    /*
     * Configure reciprocal process redistribution deliberately.
     *
     * The provenance guard must prevent process 2 from re-exporting the
     * process-1 external that process 1 created from process 2.
     */
    addProcess(
        r1,
        "2",
        "192.0.2.2",
        "process2-loopback",
        true,
        process2StaticRouteMap,
        Set.of(
            "1"),
        Map.of());

    addProcess(
        r2,
        "1",
        "192.0.2.3",
        "r2-r1",
        false,
        null,
        Set.of(),
        Map.of());

    VirtualRouter r1Vr =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter r2Vr =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    r1Vr.initForIgpComputation(
        topology);

    r2Vr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "r1",
            r1,
            "r2",
            r2);

    List<VirtualRouter> vrs =
        List.of(
            r1Vr,
            r2Vr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "r1",
            "r1-r2"),
        NodeInterfacePair.of(
            "r2",
            "r2-r1"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Ospfv3RoutingProcess r1Process1 =
        r1Vr
            .getOspfv3Processes()
            .get("1");

    Ospfv3RoutingProcess r1Process2 =
        r1Vr
            .getOspfv3Processes()
            .get("2");

    Ospfv3RoutingProcess r2Process1 =
        r2Vr
            .getOspfv3Processes()
            .get("1");

    assertThat(
        r1Process1,
        notNullValue());

    assertThat(
        r1Process2,
        notNullValue());

    assertThat(
        r2Process1,
        notNullValue());

    /*
     * Source process 2 retains its genuine locally redistributed E2.
     */
    Ospfv3ExternalType2Route6 sourceExternal =
        findType2(
            r1Process2,
            redistributedPrefix);

    assertThat(
        sourceExternal,
        notNullValue());

    assertThat(
        sourceExternal.getMetric(),
        equalTo(
            Ospfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));

    assertThat(
        sourceExternal.getTag(),
        equalTo(
            222L));

    /*
     * Process 1 re-originates process 2's route as requested by its
     * route-map.
     */
    Ospfv3ExternalType1Route6 localCrossProcess =
        findType1(
            r1Process1,
            redistributedPrefix);

    assertThat(
        localCrossProcess,
        notNullValue());

    assertThat(
        localCrossProcess.getMetric(),
        equalTo(
            40L));

    assertThat(
        localCrossProcess.getLsaMetric(),
        equalTo(
            40L));

    assertThat(
        localCrossProcess
            .getCostToAdvertiser(),
        equalTo(
            0L));

    assertThat(
        localCrossProcess.getTag(),
        equalTo(
            999L));

    /*
     * r2 receives process 1's E1:
     *
     * external LSA metric 40
     * + r2 receiving-interface cost 20
     * = total metric 60.
     */
    Ospfv3ExternalType1Route6 remote =
        findType1(
            r2Process1,
            redistributedPrefix);

    assertThat(
        remote,
        notNullValue());

    assertThat(
        remote.getMetric(),
        equalTo(
            60L));

    assertThat(
        remote.getLsaMetric(),
        equalTo(
            40L));

    assertThat(
        remote.getCostToAdvertiser(),
        equalTo(
            20L));

    assertThat(
        remote.getTag(),
        equalTo(
            999L));

    assertThat(
        remote.getAdvertiser(),
        equalTo(
            Ip.parse(
                "192.0.2.1")));

    /*
     * Reciprocal redistribution must not feed process 1's locally generated
     * cross-process external back into process 2. Process 2 therefore has
     * only its genuine source E2 for this prefix.
     */
    long process2ExternalCount =
        r1Process2
            .getRoutes()
            .stream()
            .filter(
                route ->
                    route
                        .getNetwork()
                        .equals(
                            redistributedPrefix))
            .filter(
                route ->
                    route
                            instanceof
                            Ospfv3ExternalType1Route6
                        || route
                            instanceof
                            Ospfv3ExternalType2Route6)
            .count();

    assertThat(
        process2ExternalCount,
        equalTo(
            1L));

    assertThat(
        findType1(
            r1Process2,
            redistributedPrefix),
        nullValue());

    /*
     * Withdraw the original static source. A fresh IGP convergence must
     * remove process 2's source external, process 1's cross-process
     * advertisement, and r2's learned route. Reciprocal redistribution must
     * not preserve a ghost copy.
     */
    assertThat(
        r1.getConfiguration()
            .getDefaultVrf()
            .getStaticRoutes6()
            .remove(
                sourceStatic),
        equalTo(
            true));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findType2(
            r1Process2,
            redistributedPrefix),
        nullValue());

    assertThat(
        findType1(
            r1Process1,
            redistributedPrefix),
        nullValue());

    assertThat(
        findType1(
            r2Process1,
            redistributedPrefix),
        nullValue());
  }
}
