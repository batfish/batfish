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
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for RFC 3101 NSSA translator election. */
public final class Ospfv3NssaTranslatorElectionTest {

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
      String... interfaces) {

    return Ospfv3Area.builder()
        .setNumber(number)
        .setNssa(true)
        .addInterfaces(
            List.of(interfaces))
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean redistributeConnected,
      Map<Long, Ospfv3Area> areas) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areas)
        .setRedistributeConnected(
            redistributeConnected)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3ExternalType2Route6
      findType5From(
          VirtualRouter vr,
          Prefix6 prefix,
          Ip advertiser) {

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
            .filter(
                r ->
                    ((Ospfv3ExternalType2Route6) r)
                        .getAdvertiser()
                        .equals(advertiser))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3ExternalType2Route6)
            route;
  }

  @Test
  public void testHighestReachableRouterIdAndFailover() {

    Node core =
        TestUtils.makeIosRouter(
            "core");

    Node low =
        TestUtils.makeIosRouter(
            "low");

    Node high =
        TestUtils.makeIosRouter(
            "high");

    Node asbr =
        TestUtils.makeIosRouter(
            "asbr");

    addOspfInterface(
        core,
        "core-low",
        "2001:db8:10::1/64",
        0L,
        5);

    addOspfInterface(
        low,
        "low-core",
        "2001:db8:10::2/64",
        0L,
        10);

    addOspfInterface(
        core,
        "core-high",
        "2001:db8:11::1/64",
        0L,
        5);

    addOspfInterface(
        high,
        "high-core",
        "2001:db8:11::2/64",
        0L,
        10);

    addOspfInterface(
        low,
        "low-asbr",
        "2001:db8:20::1/64",
        1L,
        20);

    addOspfInterface(
        asbr,
        "asbr-low",
        "2001:db8:20::2/64",
        1L,
        40);

    addOspfInterface(
        high,
        "high-asbr",
        "2001:db8:21::1/64",
        1L,
        30);

    addOspfInterface(
        asbr,
        "asbr-high",
        "2001:db8:21::2/64",
        1L,
        40);

    addExternal(
        asbr,
        "external",
        "2001:db8:beef::1/128");

    addProcess(
        core,
        "192.0.2.1",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-low",
                "core-high")));

    addProcess(
        low,
        "192.0.2.2",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "low-core"),
            1L,
            nssaArea(
                1L,
                "low-asbr")));

    addProcess(
        high,
        "192.0.2.200",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "high-core"),
            1L,
            nssaArea(
                1L,
                "high-asbr")));

    addProcess(
        asbr,
        "192.0.2.3",
        true,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                "asbr-low",
                "asbr-high")));

    VirtualRouter coreVr =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter lowVr =
        low.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter highVr =
        high.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbrVr =
        asbr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    coreVr.initForIgpComputation(
        topology);

    lowVr.initForIgpComputation(
        topology);

    highVr.initForIgpComputation(
        topology);

    asbrVr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "core", core,
            "low", low,
            "high", high,
            "asbr", asbr);

    List<VirtualRouter> vrs =
        List.of(
            coreVr,
            lowVr,
            highVr,
            asbrVr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-low"),
        NodeInterfacePair.of(
            "low",
            "low-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-high"),
        NodeInterfacePair.of(
            "high",
            "high-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "low",
            "low-asbr"),
        NodeInterfacePair.of(
            "asbr",
            "asbr-low"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "high",
            "high-asbr"),
        NodeInterfacePair.of(
            "asbr",
            "asbr-high"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:beef::1/128");

    Ip lowId =
        Ip.parse(
            "192.0.2.2");

    Ip highId =
        Ip.parse(
            "192.0.2.200");

    /*
     * Both ABRs are reachable through area 1 and area 0.
     * Highest router ID wins.
     */
    assertThat(
        findType5From(
            highVr,
            prefix,
            highId),
        notNullValue());

    assertThat(
        findType5From(
            lowVr,
            prefix,
            lowId),
        nullValue());

    Ospfv3ExternalType2Route6 coreRoute =
        findType5From(
            coreVr,
            prefix,
            highId);

    assertThat(
        coreRoute,
        notNullValue());

    assertThat(
        coreRoute.getAdvertiser(),
        equalTo(highId));

    /*
     * Remove the higher-ID ABR's area-0 eligibility.
     * The lower-ID ABR should take over translation.
     */
    high.getConfiguration()
        .getAllInterfaces()
        .get("high-core")
        .deactivate(
            InactiveReason.AUTOSTATE_FAILURE);

    highVr
        .updateConnectedAndLocalRoutesForAutostateChange();

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findType5From(
            highVr,
            prefix,
            highId),
        nullValue());

    assertThat(
        findType5From(
            lowVr,
            prefix,
            lowId),
        notNullValue());

    coreRoute =
        findType5From(
            coreVr,
            prefix,
            lowId);

    assertThat(
        coreRoute,
        notNullValue());

    assertThat(
        coreRoute.getAdvertiser(),
        equalTo(lowId));
  }

  @Test
  public void testPartitionedNssaElectsPerReachablePartition() {

    Node core =
        TestUtils.makeIosRouter(
            "core");

    Node low =
        TestUtils.makeIosRouter(
            "low");

    Node high =
        TestUtils.makeIosRouter(
            "high");

    Node asbrLow =
        TestUtils.makeIosRouter(
            "asbr-low");

    Node asbrHigh =
        TestUtils.makeIosRouter(
            "asbr-high");

    addOspfInterface(
        core,
        "core-low",
        "2001:db8:30::1/64",
        0L,
        5);

    addOspfInterface(
        low,
        "low-core",
        "2001:db8:30::2/64",
        0L,
        10);

    addOspfInterface(
        core,
        "core-high",
        "2001:db8:31::1/64",
        0L,
        5);

    addOspfInterface(
        high,
        "high-core",
        "2001:db8:31::2/64",
        0L,
        10);

    addOspfInterface(
        low,
        "low-nssa",
        "2001:db8:40::1/64",
        1L,
        20);

    addOspfInterface(
        asbrLow,
        "asbr-low-abr",
        "2001:db8:40::2/64",
        1L,
        40);

    addOspfInterface(
        high,
        "high-nssa",
        "2001:db8:41::1/64",
        1L,
        30);

    addOspfInterface(
        asbrHigh,
        "asbr-high-abr",
        "2001:db8:41::2/64",
        1L,
        40);

    addExternal(
        asbrLow,
        "external-low",
        "2001:db8:aaaa::1/128");

    addExternal(
        asbrHigh,
        "external-high",
        "2001:db8:bbbb::1/128");

    addProcess(
        core,
        "192.0.2.1",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-low",
                "core-high")));

    addProcess(
        low,
        "192.0.2.2",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "low-core"),
            1L,
            nssaArea(
                1L,
                "low-nssa")));

    addProcess(
        high,
        "192.0.2.200",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "high-core"),
            1L,
            nssaArea(
                1L,
                "high-nssa")));

    addProcess(
        asbrLow,
        "192.0.2.11",
        true,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                "asbr-low-abr")));

    addProcess(
        asbrHigh,
        "192.0.2.12",
        true,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                "asbr-high-abr")));

    VirtualRouter coreVr =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter lowVr =
        low.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter highVr =
        high.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbrLowVr =
        asbrLow.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter asbrHighVr =
        asbrHigh.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    coreVr.initForIgpComputation(
        topology);

    lowVr.initForIgpComputation(
        topology);

    highVr.initForIgpComputation(
        topology);

    asbrLowVr.initForIgpComputation(
        topology);

    asbrHighVr.initForIgpComputation(
        topology);

    Map<String, Node> nodes =
        ImmutableMap.of(
            "core", core,
            "low", low,
            "high", high,
            "asbr-low", asbrLow,
            "asbr-high", asbrHigh);

    List<VirtualRouter> vrs =
        List.of(
            coreVr,
            lowVr,
            highVr,
            asbrLowVr,
            asbrHighVr);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-low"),
        NodeInterfacePair.of(
            "low",
            "low-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core",
            "core-high"),
        NodeInterfacePair.of(
            "high",
            "high-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "low",
            "low-nssa"),
        NodeInterfacePair.of(
            "asbr-low",
            "asbr-low-abr"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "high",
            "high-nssa"),
        NodeInterfacePair.of(
            "asbr-high",
            "asbr-high-abr"));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 lowPrefix =
        Prefix6.parse(
            "2001:db8:aaaa::1/128");

    Prefix6 highPrefix =
        Prefix6.parse(
            "2001:db8:bbbb::1/128");

    Ip lowId =
        Ip.parse(
            "192.0.2.2");

    Ip highId =
        Ip.parse(
            "192.0.2.200");

    /*
     * The ABRs can reach each other through area 0 but not through the
     * partitioned NSSA. Each partition therefore elects a translator.
     */
    assertThat(
        findType5From(
            lowVr,
            lowPrefix,
            lowId),
        notNullValue());

    assertThat(
        findType5From(
            highVr,
            highPrefix,
            highId),
        notNullValue());

    assertThat(
        findType5From(
            coreVr,
            lowPrefix,
            lowId),
        notNullValue());

    assertThat(
        findType5From(
            coreVr,
            highPrefix,
            highId),
        notNullValue());

    assertThat(
        findType5From(
            lowVr,
            lowPrefix,
            highId),
        nullValue());

    assertThat(
        findType5From(
            highVr,
            highPrefix,
            lowId),
        nullValue());
  }
}
