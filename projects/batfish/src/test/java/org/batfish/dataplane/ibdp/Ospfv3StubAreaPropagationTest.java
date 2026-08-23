package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** Integration tests for OSPFv3 stub and totally-stubby areas. */
public final class Ospfv3StubAreaPropagationTest {

  private static final class TestL3Adjacencies
      implements L3Adjacencies {

    private final Map<
            NodeInterfacePair,
            NodeInterfacePair>
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
      return Optional
          .ofNullable(_pairs.get(i1))
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
                .setPassive(true)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    OspfNetworkType.BROADCAST)
                .build())
        .build();
  }

  private static Interface addExternalConnected(
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

  private static Ospfv3Area area(
      long number,
      boolean stub,
      boolean suppressInterArea,
      String... interfaces) {
    return area(
        number,
        stub,
        suppressInterArea,
        Ospfv3Area.DEFAULT_STUB_DEFAULT_METRIC,
        interfaces);
  }

  private static Ospfv3Area area(
      long number,
      boolean stub,
      boolean suppressInterArea,
      long defaultMetric,
      String... interfaces) {

    Ospfv3Area.Builder builder =
        Ospfv3Area.builder()
            .setNumber(number)
            .setDefaultMetric(
                defaultMetric);

    for (String iface : interfaces) {
      builder.addInterface(iface);
    }

    if (stub) {
      builder
          .setStub(true)
          .setSuppressInterArea(
              suppressInterArea);
    }

    return builder.build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean redistributeConnected,
      Ospfv3Area... areas) {

    ImmutableMap.Builder<Long, Ospfv3Area>
        areaMap =
            ImmutableMap.builder();

    for (Ospfv3Area area : areas) {
      areaMap.put(
          area.getAreaNumber(),
          area);
    }

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(areaMap.build())
        .setRedistributeConnected(
            redistributeConnected)
        .setRedistributionMetric(25L)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3InterAreaRoute6
      findInter(
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
        : (Ospfv3ExternalType2Route6) route;
  }

  @Test
  public void testStubAndTotallyStubbyPropagation() {
    Node core =
        TestUtils.makeIosRouter("core");
    Node abr =
        TestUtils.makeIosRouter("abr");
    Node stub =
        TestUtils.makeIosRouter("stub");
    Node totallyStubby =
        TestUtils.makeIosRouter(
            "totally-stubby");

    /*
     * Backbone.
     */
    addOspfInterface(
        core,
        "core-abr",
        "2001:db8:0::1/64",
        0L,
        10);

    addOspfLoopback(
        core,
        "loopback0",
        "2001:db8:100::1/128",
        0L);

    /*
     * Connected, but not OSPF-enabled. Core redistributes this as E2.
     */
    addExternalConnected(
        core,
        "external0",
        "2001:db8:eeee::1/128");

    addOspfInterface(
        abr,
        "abr-core",
        "2001:db8:0::2/64",
        0L,
        10);

    /*
     * Ordinary stub area 1.
     */
    addOspfInterface(
        abr,
        "abr-stub",
        "2001:db8:1::1/64",
        1L,
        10);

    addOspfInterface(
        stub,
        "stub-abr",
        "2001:db8:1::2/64",
        1L,
        10);

    /*
     * Totally-stubby area 2. no-summary is an ABR-side policy.
     * The internal router simply knows that area 2 is a stub.
     */
    addOspfInterface(
        abr,
        "abr-total",
        "2001:db8:2::1/64",
        2L,
        10);

    addOspfInterface(
        totallyStubby,
        "total-abr",
        "2001:db8:2::2/64",
        2L,
        10);

    addProcess(
        core,
        "192.0.2.1",
        true,
        area(
            0L,
            false,
            false,
            "core-abr",
            "loopback0"));

    addProcess(
        abr,
        "192.0.2.2",
        false,
        area(
            0L,
            false,
            false,
            "abr-core"),
        area(
            1L,
            true,
            false,
            7L,
            "abr-stub"),
        area(
            2L,
            true,
            true,
            11L,
            "abr-total"));

    addProcess(
        stub,
        "192.0.2.3",
        false,
        area(
            1L,
            true,
            false,
            "stub-abr"));

    addProcess(
        totallyStubby,
        "192.0.2.4",
        false,
        area(
            2L,
            true,
            false,
            "total-abr"));

    VirtualRouter vrCore =
        core.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vrAbr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vrStub =
        stub.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vrTotallyStubby =
        totallyStubby
            .getVirtualRouterOrThrow(
                Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vrCore.initForIgpComputation(topology);
    vrAbr.initForIgpComputation(topology);
    vrStub.initForIgpComputation(topology);
    vrTotallyStubby
        .initForIgpComputation(topology);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "core", "core-abr"),
        NodeInterfacePair.of(
            "abr", "abr-core"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr", "abr-stub"),
        NodeInterfacePair.of(
            "stub", "stub-abr"));

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr", "abr-total"),
        NodeInterfacePair.of(
            "totally-stubby",
            "total-abr"));

    int iterations =
        IncrementalBdpEngine
            .initOspfv3InternalRoutes(
                ImmutableMap.of(
                    "core", core,
                    "abr", abr,
                    "stub", stub,
                    "totally-stubby",
                    totallyStubby),
                java.util.List.of(
                    vrCore,
                    vrAbr,
                    vrStub,
                    vrTotallyStubby),
                adjacencies);

    assertThat(
        iterations > 0,
        equalTo(true));

    Prefix6 internal =
        Prefix6.parse(
            "2001:db8:100::1/128");

    Prefix6 external =
        Prefix6.parse(
            "2001:db8:eeee::1/128");

    /*
     * Ordinary stub:
     *   - receives inter-area summaries
     *   - receives ABR default
     *   - does not receive external routes
     */
    Ospfv3InterAreaRoute6 stubInternal =
        findInter(vrStub, internal);

    Ospfv3InterAreaRoute6 stubDefault =
        findInter(
            vrStub,
            Prefix6.ZERO);

    assertThat(
        stubInternal,
        notNullValue());

    assertThat(
        stubDefault,
        notNullValue());

    assertThat(
        stubDefault.getMetric(),
        equalTo(17L));

    assertThat(
        findExternal(
            vrStub,
            external),
        nullValue());

    assertThat(
        vrStub.getMainRib6()
            .getRoutes(Prefix6.ZERO),
        hasItem(stubDefault));

    /*
     * Totally-stubby:
     *   - receives only the ABR default
     *   - does not receive other inter-area summaries
     *   - does not receive external routes
     */
    Ospfv3InterAreaRoute6 totalDefault =
        findInter(
            vrTotallyStubby,
            Prefix6.ZERO);

    assertThat(
        totalDefault,
        notNullValue());

    assertThat(
        totalDefault.getMetric(),
        equalTo(21L));

    assertThat(
        findInter(
            vrTotallyStubby,
            internal),
        nullValue());

    assertThat(
        findExternal(
            vrTotallyStubby,
            external),
        nullValue());

    assertThat(
        vrTotallyStubby
            .getMainRib6()
            .getRoutes(Prefix6.ZERO),
        hasItem(totalDefault));
  }
}
