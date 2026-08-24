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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3Authentication;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 IPsec AH adjacency compatibility. */
public final class Ospfv3AuthenticationAdjacencyTest {

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

  private static Ospfv3Authentication auth(
      String key) {

    return new Ospfv3Authentication(
        256L,
        Ospfv3Authentication.AuthType.SHA1,
        Ospfv3Authentication.KeyType.PLAINTEXT,
        key);
  }

  private static Ospfv3InterfaceSettings settings(
      boolean passive,
      int cost,
      Ospfv3Authentication authentication) {

    return Ospfv3InterfaceSettings.builder()
        .setAreaName(0L)
        .setAuthentication(
            authentication)
        .setCost(cost)
        .setProcess("1")
        .setEnabled(true)
        .setPassive(passive)
        .setHelloInterval(10)
        .setDeadInterval(40)
        .setNetworkType(
            OspfNetworkType.POINT_TO_POINT)
        .build();
  }

  private static Ospfv3InterfaceSettings settingsWithoutAuth(
      boolean passive,
      int cost) {

    return Ospfv3InterfaceSettings.builder()
        .setAreaName(0L)
        .setCost(cost)
        .setProcess("1")
        .setEnabled(true)
        .setPassive(passive)
        .setHelloInterval(10)
        .setDeadInterval(40)
        .setNetworkType(
            OspfNetworkType.POINT_TO_POINT)
        .build();
  }

  private static Interface addInterface(
      Node node,
      String name,
      String address,
      InterfaceType type,
      Ospfv3InterfaceSettings settings) {

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
            settings)
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      String... interfaces) {

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(
                routerId))
        .setAreas(
            ImmutableMap.of(
                0L,
                Ospfv3Area.builder()
                    .setNumber(0L)
                    .addInterfaces(
                        List.of(
                            interfaces))
                    .build()))
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3IntraAreaRoute6 findRoute(
      VirtualRouter vr,
      Prefix6 prefix) {

    AbstractRoute6 route =
        vr.getOspfv3Processes()
            .get("1")
            .getRoutes()
            .stream()
            .filter(
                candidate ->
                    candidate
                        instanceof
                        Ospfv3IntraAreaRoute6)
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
        : (Ospfv3IntraAreaRoute6)
            route;
  }

  @Test
  public void testAuthenticationControlsAdjacencyAndWithdrawal() {

    Node r1 =
        TestUtils.makeIosRouter(
            "r1");

    Node r2 =
        TestUtils.makeIosRouter(
            "r2");

    Ospfv3Authentication shared =
        auth(
            "shared-secret");

    Interface r1Link =
        addInterface(
            r1,
            "r1-r2",
            "2001:db8:12::1/64",
            InterfaceType.PHYSICAL,
            settings(
                false,
                10,
                shared));

    Interface r2Link =
        addInterface(
            r2,
            "r2-r1",
            "2001:db8:12::2/64",
            InterfaceType.PHYSICAL,
            settings(
                false,
                20,
                shared));

    addInterface(
        r1,
        "r1-loopback",
        "2001:db8:ffff::1/128",
        InterfaceType.LOOPBACK,
        settingsWithoutAuth(
            true,
            1));

    addProcess(
        r1,
        "192.0.2.1",
        "r1-r2",
        "r1-loopback");

    addProcess(
        r2,
        "192.0.2.2",
        "r2-r1");

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

    Prefix6 sourceLoopback =
        Prefix6.parse(
            "2001:db8:ffff::1/128");

    /*
     * Matching SPI, algorithm, key representation, and key form a normal
     * OSPFv3 adjacency.
     *
     * Remote loopback metric 1 + r2 receiving-interface cost 20 = 21.
     */
    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    Ospfv3IntraAreaRoute6 learned =
        findRoute(
            r2Vr,
            sourceLoopback);

    assertThat(
        learned,
        notNullValue());

    assertThat(
        learned.getMetric(),
        equalTo(21L));

    assertThat(
        learned.getNextHopInterface(),
        equalTo(
            "r2-r1"));

    /*
     * Same SPI and SHA1 algorithm but a different key must prevent the
     * adjacency. A fresh convergence withdraws the previously learned route.
     */
    r2Link.setOspfv3Settings(
        settings(
            false,
            20,
            auth(
                "wrong-secret")));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findRoute(
            r2Vr,
            sourceLoopback),
        nullValue());

    /*
     * Null/no authentication is also incompatible with an authenticated
     * neighbor.
     */
    r2Link.setOspfv3Settings(
        settingsWithoutAuth(
            false,
            20));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findRoute(
            r2Vr,
            sourceLoopback),
        nullValue());

    /*
     * Restore matching authentication and the adjacency/route return.
     */
    r2Link.setOspfv3Settings(
        settings(
            false,
            20,
            shared));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            vrs,
            adjacencies);

    assertThat(
        findRoute(
            r2Vr,
            sourceLoopback),
        notNullValue());

    /*
     * Keep the source interface referenced so Error Prone also verifies the
     * intended authenticated endpoint remains configured.
     */
    assertThat(
        r1Link
            .getOspfv3Settings()
            .getAuthentication(),
        equalTo(
            shared));
  }
}
