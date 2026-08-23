package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 NSSA behavior. */
public final class Ospfv3NssaPropagationTest {

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
        .setBandwidth(10_000_000_000D)
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
                    OspfNetworkType.POINT_TO_POINT)
                .build())
        .build();
  }

  private static Interface addOspfLoopback(
      Node node,
      String name,
      String address,
      long area) {

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
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(area)
                .setCost(1)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    OspfNetworkType.BROADCAST)
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

  private static Ospfv3Area normalArea(
      long area,
      String... interfaces) {
    return Ospfv3Area.builder()
        .setNumber(area)
        .addInterfaces(
            List.of(interfaces))
        .build();
  }

  private static Ospfv3Area nssaArea(
      long area,
      boolean noSummary,
      long defaultMetric,
      String... interfaces) {
    return Ospfv3Area.builder()
        .setNumber(area)
        .addInterfaces(
            List.of(interfaces))
        .setNssa(true)
        .setSuppressInterArea(
            noSummary)
        .setDefaultMetric(
            defaultMetric)
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

  private static Ospfv3NssaExternalType2Route6
      findNssaExternal(
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
      findType5External(
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
        : (Ospfv3InterAreaRoute6) route;
  }

  @Test
  public void testNssaTranslationSuppressionAndWithdrawal() {
    Node core =
        TestUtils.makeIosRouter("core");

    Node abr =
        TestUtils.makeIosRouter("abr");

    Node asbr =
        TestUtils.makeIosRouter("asbr");

    addOspfInterface(
        core,
        "core-abr",
        "2001:db8:10::1/64",
        0L,
        10);

    addOspfLoopback(
        core,
        "core-loop",
        "2001:db8:100::1/128",
        0L);

    addConnectedLoopback(
        core,
        "core-external",
        "2001:db8:cafe::1/128");

    addOspfInterface(
        abr,
        "abr-core",
        "2001:db8:10::2/64",
        0L,
        20);

    addOspfInterface(
        abr,
        "abr-nssa",
        "2001:db8:20::1/64",
        1L,
        30);

    addOspfInterface(
        asbr,
        "asbr-abr",
        "2001:db8:20::2/64",
        1L,
        40);

    addConnectedLoopback(
        asbr,
        "asbr-external",
        "2001:db8:beef::1/128");

    addProcess(
        core,
        "192.0.2.1",
        true,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "core-abr",
                "core-loop")));

    /*
     * The ABR marks area 1 NSSA no-summary and advertises
     * its default summary with metric 5.
     */
    addProcess(
        abr,
        "192.0.2.2",
        false,
        ImmutableMap.of(
            0L,
            normalArea(
                0L,
                "abr-core"),
            1L,
            nssaArea(
                1L,
                true,
                5L,
                "abr-nssa")));

    /*
     * no-summary is an ABR advertisement policy, not an
     * adjacency compatibility bit. The internal ASBR simply
     * identifies area 1 as NSSA.
     */
    addProcess(
        asbr,
        "192.0.2.3",
        true,
        ImmutableMap.of(
            1L,
            nssaArea(
                1L,
                false,
                1L,
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
        TopologyContext.builder().build();

    coreVr.initForIgpComputation(topology);
    abrVr.initForIgpComputation(topology);
    asbrVr.initForIgpComputation(topology);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core", "core-abr"),
        NodeInterfacePair.of(
            "abr", "abr-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr", "abr-nssa"),
        NodeInterfacePair.of(
            "asbr", "asbr-abr"));

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

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Prefix6 nssaExternal =
        Prefix6.parse(
            "2001:db8:beef::1/128");

    Prefix6 coreExternal =
        Prefix6.parse(
            "2001:db8:cafe::1/128");

    Prefix6 coreInternal =
        Prefix6.parse(
            "2001:db8:100::1/128");

    /*
     * The ASBR originates its redistributed route as NSSA Type-7/N2.
     */
    Ospfv3NssaExternalType2Route6
        localType7 =
            findNssaExternal(
                asbrVr,
                nssaExternal);

    assertThat(
        localType7,
        notNullValue());

    assertThat(
        localType7.getArea(),
        equalTo(1L));

    assertThat(
        localType7.getMetric(),
        equalTo(
            Ospfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));

    /*
     * ABR learns the N2 inside area 1.
     */
    Ospfv3NssaExternalType2Route6
        abrType7 =
            findNssaExternal(
                abrVr,
                nssaExternal);

    assertThat(
        abrType7,
        notNullValue());

    assertThat(
        abrType7.getCostToAdvertiser(),
        equalTo(30L));

    assertThat(
        abrVr.getMainRib6()
            .getRoutes(nssaExternal),
        hasItem(abrType7));

    /*
     * ABR translates Type-7 to Type-5 toward area 0.
     */
    Ospfv3ExternalType2Route6
        translated =
            findType5External(
                coreVr,
                nssaExternal);

    assertThat(
        translated,
        notNullValue());

    assertThat(
        translated.getMetric(),
        equalTo(
            Ospfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));

    assertThat(
        translated.getAdvertiser(),
        equalTo(
            Ip.parse("192.0.2.2")));

    /*
     * Type-7 itself never leaks into the normal backbone.
     */
    assertThat(
        findNssaExternal(
            coreVr,
            nssaExternal),
        nullValue());

    /*
     * Type-5 from the backbone does not enter the NSSA.
     */
    assertThat(
        findType5External(
            asbrVr,
            coreExternal),
        nullValue());

    /*
     * NSSA no-summary suppresses the ordinary inter-area summary.
     */
    assertThat(
        findInterArea(
            asbrVr,
            coreInternal),
        nullValue());

    /*
     * But the ABR still injects the NSSA default summary.
     * ASBR interface cost 40 + ABR default metric 5 = 45.
     */
    Ospfv3InterAreaRoute6 defaultRoute =
        findInterArea(
            asbrVr,
            Prefix6.ZERO);

    assertThat(
        defaultRoute,
        notNullValue());

    assertThat(
        defaultRoute.getMetric(),
        equalTo(45L));

    /*
     * Remove the redistributed source from the NSSA ASBR.
     * Both Type-7 and translated Type-5 must withdraw.
     */
    asbr.getConfiguration()
        .getAllInterfaces()
        .get("asbr-external")
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
        findNssaExternal(
            abrVr,
            nssaExternal),
        nullValue());

    assertThat(
        findType5External(
            coreVr,
            nssaExternal),
        nullValue());

    assertThat(
        coreVr.getMainRib6()
            .getRoutes(nssaExternal),
        empty());
  }
}
