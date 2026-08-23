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
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 inter-area range aggregation. */
public final class Ospfv3AreaRangePropagationTest {

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
      boolean passive) {

    Configuration c =
        node.getConfiguration();

    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(
            c.getDefaultVrf())
        .setType(
            passive
                ? InterfaceType.LOOPBACK
                : InterfaceType.PHYSICAL)
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
                    passive
                        ? OspfNetworkType.BROADCAST
                        : OspfNetworkType.POINT_TO_POINT)
                .setPassive(passive)
                .setPriority(1)
                .setProcess("1")
                .setRetransmitInterval(5)
                .setTransitDelay(1)
                .build())
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
  public void testSummarizationNoAdvertiseAndWithdrawal() {

    Node r1 =
        TestUtils.makeIosRouter("r1");

    Node abr =
        TestUtils.makeIosRouter("abr");

    Node r2 =
        TestUtils.makeIosRouter("r2");

    /*
     * Area 1 between r1 and the ABR.
     */
    addInterface(
        r1,
        "r1-area1",
        "2001:db8:1::1/64",
        1L,
        5,
        false);

    addInterface(
        abr,
        "abr-area1",
        "2001:db8:1::2/64",
        1L,
        10,
        false);

    /*
     * Two advertised component routes inside 2001:db8:100::/48.
     * Their metrics at the ABR become 11 and 17.
     */
    addInterface(
        r1,
        "loop-low",
        "2001:db8:100:1::1/128",
        1L,
        1,
        true);

    addInterface(
        r1,
        "loop-high",
        "2001:db8:100:2::1/128",
        1L,
        7,
        true);

    /*
     * This component falls inside a no-advertise range.
     */
    addInterface(
        r1,
        "loop-hidden",
        "2001:db8:200:1::1/128",
        1L,
        3,
        true);

    /*
     * Area 0 between the ABR and r2.
     */
    addInterface(
        abr,
        "abr-backbone",
        "2001:db8:0::1/64",
        0L,
        15,
        false);

    addInterface(
        r2,
        "r2-backbone",
        "2001:db8:0::2/64",
        0L,
        20,
        false);

    Ospfv3Area r1Area =
        Ospfv3Area.builder()
            .setNumber(1L)
            .addInterfaces(
                List.of(
                    "r1-area1",
                    "loop-low",
                    "loop-high",
                    "loop-hidden"))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.1"))
        .setAreas(
            ImmutableMap.of(
                1L,
                r1Area))
        .setVrf(
            r1.getConfiguration()
                .getDefaultVrf())
        .build();

    Ospfv3Area abrArea1 =
        Ospfv3Area.builder()
            .setNumber(1L)
            .addInterface(
                "abr-area1")
            .addRange(
                new Ospfv3AreaRange(
                    Prefix6.parse(
                        "2001:db8:100::/48"),
                    Ospfv3AreaRange.Type.INTER_AREA,
                    true))
            .addRange(
                new Ospfv3AreaRange(
                    Prefix6.parse(
                        "2001:db8:200::/48"),
                    Ospfv3AreaRange.Type.INTER_AREA,
                    false))
            .build();

    Ospfv3Area abrArea0 =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface(
                "abr-backbone")
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.10"))
        .setAreas(
            ImmutableMap.of(
                0L,
                abrArea0,
                1L,
                abrArea1))
        .setVrf(
            abr.getConfiguration()
                .getDefaultVrf())
        .build();

    Ospfv3Area r2Area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface(
                "r2-backbone")
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                "192.0.2.2"))
        .setAreas(
            ImmutableMap.of(
                0L,
                r2Area))
        .setVrf(
            r2.getConfiguration()
                .getDefaultVrf())
        .build();

    VirtualRouter vrR1 =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vrAbr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vrR2 =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    vrR1.initForIgpComputation(
        topology);

    vrAbr.initForIgpComputation(
        topology);

    vrR2.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "r1",
            r1,
            "abr",
            abr,
            "r2",
            r2);

    List<VirtualRouter> vrs =
        List.of(
            vrR1,
            vrAbr,
            vrR2);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "r1",
            "r1-area1"),
        NodeInterfacePair.of(
            "abr",
            "abr-area1"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr",
            "abr-backbone"),
        NodeInterfacePair.of(
            "r2",
            "r2-backbone"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 summary =
        Prefix6.parse(
            "2001:db8:100::/48");

    Prefix6 low =
        Prefix6.parse(
            "2001:db8:100:1::1/128");

    Prefix6 high =
        Prefix6.parse(
            "2001:db8:100:2::1/128");

    Prefix6 hidden =
        Prefix6.parse(
            "2001:db8:200:1::1/128");

    /*
     * r2 receives one summary instead of either component specific.
     *
     * Highest component metric at ABR:
     *
     *   loop-high                     7
     * + ABR area-1 receive cost      10
     *                                --
     *                                17
     *
     * r2 then adds its backbone receive cost of 20:
     *
     *                                37
     */
    Ospfv3InterAreaRoute6 summaryRoute =
        findInterArea(
            vrR2,
            summary);

    assertThat(
        summaryRoute,
        notNullValue());

    assertThat(
        summaryRoute.getMetric(),
        equalTo(37L));

    assertThat(
        findInterArea(
            vrR2,
            low),
        nullValue());

    assertThat(
        findInterArea(
            vrR2,
            high),
        nullValue());

    /*
     * no-advertise suppresses both the configured range and
     * matching specifics.
     */
    assertThat(
        findInterArea(
            vrR2,
            Prefix6.parse(
                "2001:db8:200::/48")),
        nullValue());

    assertThat(
        findInterArea(
            vrR2,
            hidden),
        nullValue());

    /*
     * Remove the higher-cost component. The summary remains, but its
     * metric is recomputed from the surviving component:
     *
     *   1 + 10 + 20 = 31
     */
    r1.getConfiguration()
        .getAllInterfaces()
        .get("loop-high")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    vrR1
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    summaryRoute =
        findInterArea(
            vrR2,
            summary);

    assertThat(
        summaryRoute,
        notNullValue());

    assertThat(
        summaryRoute.getMetric(),
        equalTo(31L));

    /*
     * Remove the final component. The summary must withdraw.
     */
    r1.getConfiguration()
        .getAllInterfaces()
        .get("loop-low")
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
        findInterArea(
            vrR2,
            summary),
        nullValue());
  }
}
