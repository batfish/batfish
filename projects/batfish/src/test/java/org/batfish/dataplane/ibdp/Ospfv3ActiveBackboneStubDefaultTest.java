package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
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
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/**
 * End-to-end tests for AOS-CX active-backbone stub-default-route
 * behavior.
 */
public final class Ospfv3ActiveBackboneStubDefaultTest {

  private enum BackboneEvidence {
    LOOPBACK,
    PASSIVE_INTERFACE,
    NEIGHBOR
  }

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

  private static Interface addInterface(
      Node node,
      String name,
      String address,
      long area,
      int cost,
      InterfaceType type,
      boolean passive,
      OspfNetworkType networkType) {

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
                .setAreaName(area)
                .setCost(cost)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(passive)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    networkType)
                .build())
        .build();
  }

  private static Interface addPointToPoint(
      Node node,
      String name,
      String address,
      long area,
      int cost) {

    return addInterface(
        node,
        name,
        address,
        area,
        cost,
        InterfaceType.PHYSICAL,
        false,
        OspfNetworkType.POINT_TO_POINT);
  }

  private static Interface addBackboneLoopback(
      Node node,
      String name,
      String address) {

    /*
     * Keep this loopback non-passive so the active-backbone knob,
     * rather than passive-interface behavior, is what qualifies it.
     */
    return addInterface(
        node,
        name,
        address,
        0L,
        1,
        InterfaceType.LOOPBACK,
        false,
        OspfNetworkType.BROADCAST);
  }

  private static Interface addPassiveBackbone(
      Node node,
      String name,
      String address) {

    return addInterface(
        node,
        name,
        address,
        0L,
        1,
        InterfaceType.PHYSICAL,
        true,
        OspfNetworkType.BROADCAST);
  }

  private static Ospfv3Area area(
      long number,
      boolean stub,
      long defaultMetric,
      String... interfaces) {

    Ospfv3Area.Builder builder =
        Ospfv3Area.builder()
            .setNumber(number)
            .setDefaultMetric(
                defaultMetric);

    for (String iface :
        interfaces) {

      builder.addInterface(
          iface);
    }

    if (stub) {
      builder.setStub(true);
    }

    return builder.build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      boolean activeBackboneStubDefaultRoute,
      Ospfv3Area... areas) {

    ImmutableMap.Builder<
            Long,
            Ospfv3Area>
        areaMap =
            ImmutableMap.builder();

    for (Ospfv3Area area :
        areas) {

      areaMap.put(
          area.getAreaNumber(),
          area);
    }

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                routerId))
        .setAreas(
            areaMap.build())
        .setActiveBackboneStubDefaultRoute(
            activeBackboneStubDefaultRoute)
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3InterAreaRoute6
      findDefault(
          VirtualRouter vr) {

    AbstractRoute6 route =
        vr.getOspfv3Processes()
            .get("1")
            .getRoutes()
            .stream()
            .filter(
                candidate ->
                    candidate
                        instanceof
                        Ospfv3InterAreaRoute6)
            .filter(
                candidate ->
                    candidate
                        .getNetwork()
                        .equals(
                            Prefix6.ZERO))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3InterAreaRoute6)
            route;
  }

  private static Ospfv3InterAreaRoute6
      computeDefault(
          boolean activeBackboneStubDefaultRoute,
          BackboneEvidence evidence) {

    Node abr =
        TestUtils.makeIosRouter(
            "abr");

    Node stub =
        TestUtils.makeIosRouter(
            "stub");

    Node core =
        evidence
                == BackboneEvidence.NEIGHBOR
            ? TestUtils.makeIosRouter(
                "core")
            : null;

    /*
     * Restricted area 1:
     *
     * abr ---- stub
     */
    addPointToPoint(
        abr,
        "abr-stub",
        "2001:db8:1::1/64",
        1L,
        10);

    addPointToPoint(
        stub,
        "stub-abr",
        "2001:db8:1::2/64",
        1L,
        10);

    String backboneInterface;

    switch (evidence) {

      case LOOPBACK -> {

        backboneInterface =
            "backbone-loopback";

        addBackboneLoopback(
            abr,
            backboneInterface,
            "2001:db8:100::1/128");
      }

      case PASSIVE_INTERFACE -> {

        backboneInterface =
            "backbone-passive";

        addPassiveBackbone(
            abr,
            backboneInterface,
            "2001:db8:200::1/64");
      }

      case NEIGHBOR -> {

        backboneInterface =
            "abr-core";

        addPointToPoint(
            abr,
            "abr-core",
            "2001:db8:0::2/64",
            0L,
            10);

        addPointToPoint(
            core,
            "core-abr",
            "2001:db8:0::1/64",
            0L,
            10);
      }

      default ->
          throw new IllegalStateException();
    }

    addProcess(
        abr,
        "192.0.2.1",
        activeBackboneStubDefaultRoute,
        area(
            0L,
            false,
            1L,
            backboneInterface),
        area(
            1L,
            true,
            7L,
            "abr-stub"));

    addProcess(
        stub,
        "192.0.2.2",
        true,
        area(
            1L,
            true,
            7L,
            "stub-abr"));

    if (core != null) {

      addProcess(
          core,
          "192.0.2.3",
          true,
          area(
              0L,
              false,
              1L,
              "core-abr"));
    }

    VirtualRouter abrVr =
        abr.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter stubVr =
        stub.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter coreVr =
        core == null
            ? null
            : core.getVirtualRouterOrThrow(
                Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    abrVr.initForIgpComputation(
        topology);

    stubVr.initForIgpComputation(
        topology);

    if (coreVr != null) {
      coreVr.initForIgpComputation(
          topology);
    }

    Map<String, Node> nodes =
        new HashMap<>();

    nodes.put(
        "abr",
        abr);

    nodes.put(
        "stub",
        stub);

    if (core != null) {
      nodes.put(
          "core",
          core);
    }

    List<VirtualRouter> vrs =
        new ArrayList<>();

    vrs.add(
        abrVr);

    vrs.add(
        stubVr);

    if (coreVr != null) {
      vrs.add(
          coreVr);
    }

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "abr",
            "abr-stub"),
        NodeInterfacePair.of(
            "stub",
            "stub-abr"));

    if (core != null) {

      adjacencies.addPair(
          NodeInterfacePair.of(
              "abr",
              "abr-core"),
          NodeInterfacePair.of(
              "core",
              "core-abr"));
    }

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    return findDefault(
        stubVr);
  }

  @Test
  public void testLoopbackHonorsActiveBackboneKnob() {

    Ospfv3InterAreaRoute6 enabled =
        computeDefault(
            true,
            BackboneEvidence.LOOPBACK);

    assertThat(
        enabled,
        notNullValue());

    /*
     * ABR's stub default metric = 7.
     * Stub's receiving-interface cost = 10.
     */
    assertThat(
        enabled.getMetric(),
        equalTo(17L));

    assertThat(
        enabled.getNextHopInterface(),
        equalTo(
            "stub-abr"));

    Ospfv3InterAreaRoute6 disabled =
        computeDefault(
            false,
            BackboneEvidence.LOOPBACK);

    assertThat(
        disabled,
        nullValue());
  }

  @Test
  public void testPassiveBackboneOverridesDisabledKnob() {

    Ospfv3InterAreaRoute6 route =
        computeDefault(
            false,
            BackboneEvidence.PASSIVE_INTERFACE);

    assertThat(
        route,
        notNullValue());

    assertThat(
        route.getMetric(),
        equalTo(17L));
  }

  @Test
  public void testBackboneNeighborOverridesDisabledKnob() {

    Ospfv3InterAreaRoute6 route =
        computeDefault(
            false,
            BackboneEvidence.NEIGHBOR);

    assertThat(
        route,
        notNullValue());

    assertThat(
        route.getMetric(),
        equalTo(17L));
  }
}
