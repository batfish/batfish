package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
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
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** End-to-end tests for OSPFv3 broadcast DR/BDR behavior. */
public final class Ospfv3BroadcastElectionTest {

  private static final class BroadcastL3Adjacencies
      implements L3Adjacencies {

    BroadcastL3Adjacencies(
        Set<NodeInterfacePair> members) {
      _members = new HashSet<>(members);
    }

    @Override
    public boolean inSameBroadcastDomain(
        NodeInterfacePair i1,
        NodeInterfacePair i2) {
      return !i1.equals(i2)
          && _members.contains(i1)
          && _members.contains(i2);
    }

    @Override
    public Optional<NodeInterfacePair>
        pairedPointToPointL3Interface(
            NodeInterfacePair iface) {
      return Optional.empty();
    }

    private final Set<NodeInterfacePair> _members;
  }

  private static Interface addLan(
      Node node,
      String name,
      String address,
      int cost,
      int priority) {

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
                .setAreaName(0L)
                .setCost(cost)
                .setDeadInterval(40)
                .setEnabled(true)
                .setHelloInterval(10)
                .setNetworkType(
                    OspfNetworkType.BROADCAST)
                .setPassive(false)
                .setPriority(priority)
                .setProcess("1")
                .setRetransmitInterval(5)
                .setTransitDelay(1)
                .build())
        .build();
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
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(0L)
                .setCost(1)
                .setDeadInterval(40)
                .setEnabled(true)
                .setHelloInterval(10)
                .setNetworkType(
                    OspfNetworkType.BROADCAST)
                .setPassive(true)
                .setPriority(1)
                .setProcess("1")
                .setRetransmitInterval(5)
                .setTransitDelay(1)
                .build())
        .build();
  }

  private static void addProcess(
      Node node,
      String routerId,
      String lan,
      String loopback) {

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterfaces(
                List.of(
                    lan,
                    loopback))
            .build();

    Ospfv3Process.builder()
        .setProcessId("1")
        .setRouterId(
            Ip.parse(routerId))
        .setAreas(
            ImmutableMap.of(
                0L,
                area))
        .setVrf(
            node.getConfiguration()
                .getDefaultVrf())
        .build();
  }

  private static Ospfv3IntraAreaRoute6
      findIntraAreaRoute(
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
                        Ospfv3IntraAreaRoute6)
            .filter(
                r ->
                    r.getNetwork()
                        .equals(prefix))
            .findFirst()
            .orElse(null);

    return route == null
        ? null
        : (Ospfv3IntraAreaRoute6)
            route;
  }

  @Test
  public void testElectionAndDrotherFlooding() {
    Node r1 =
        TestUtils.makeIosRouter("r1");

    Node r2 =
        TestUtils.makeIosRouter("r2");

    Node r3 =
        TestUtils.makeIosRouter("r3");

    Node r4 =
        TestUtils.makeIosRouter("r4");

    /*
     * r1 and r2 have equal highest priority.
     * r2's higher router ID makes it DR.
     * r1 becomes BDR.
     *
     * r3 has the highest router ID but priority 0, so it is
     * ineligible for either role.
     */
    addLan(
        r1,
        "r1-lan",
        "2001:db8:10::1/64",
        10,
        100);

    addLan(
        r2,
        "r2-lan",
        "2001:db8:10::2/64",
        20,
        100);

    addLan(
        r3,
        "r3-lan",
        "2001:db8:10::3/64",
        30,
        0);

    addLan(
        r4,
        "r4-lan",
        "2001:db8:10::4/64",
        40,
        1);

    addLoopback(
        r1,
        "r1-loop",
        "2001:db8:1::1/128");

    addLoopback(
        r2,
        "r2-loop",
        "2001:db8:2::2/128");

    addLoopback(
        r3,
        "r3-loop",
        "2001:db8:3::3/128");

    addLoopback(
        r4,
        "r4-loop",
        "2001:db8:4::4/128");

    addProcess(
        r1,
        "192.0.2.1",
        "r1-lan",
        "r1-loop");

    addProcess(
        r2,
        "192.0.2.2",
        "r2-lan",
        "r2-loop");

    addProcess(
        r3,
        "192.0.2.254",
        "r3-lan",
        "r3-loop");

    addProcess(
        r4,
        "192.0.2.4",
        "r4-lan",
        "r4-loop");

    VirtualRouter vr1 =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr2 =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr3 =
        r3.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr4 =
        r4.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(topology);
    vr2.initForIgpComputation(topology);
    vr3.initForIgpComputation(topology);
    vr4.initForIgpComputation(topology);

    NodeInterfacePair r1Lan =
        NodeInterfacePair.of(
            "r1",
            "r1-lan");

    NodeInterfacePair r2Lan =
        NodeInterfacePair.of(
            "r2",
            "r2-lan");

    NodeInterfacePair r3Lan =
        NodeInterfacePair.of(
            "r3",
            "r3-lan");

    NodeInterfacePair r4Lan =
        NodeInterfacePair.of(
            "r4",
            "r4-lan");

    BroadcastL3Adjacencies adjacencies =
        new BroadcastL3Adjacencies(
            Set.of(
                r1Lan,
                r2Lan,
                r3Lan,
                r4Lan));

    Map<String, Node> nodes =
        ImmutableMap.of(
            "r1", r1,
            "r2", r2,
            "r3", r3,
            "r4", r4);

    /*
     * Inspect the cold-start election directly.
     */
    Ospfv3RoutingProcess.BroadcastElection
        election =
            vr1.getOspfv3Processes()
                .get("1")
                .electBroadcastDesignatedRouters(
                    r1.getConfiguration()
                        .getAllInterfaces()
                        .get("r1-lan"),
                    r1Lan,
                    nodes,
                    adjacencies);

    assertThat(
        election.getDr(),
        equalTo(r2Lan));

    assertThat(
        election.getBdr(),
        equalTo(r1Lan));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            List.of(
                vr1,
                vr2,
                vr3,
                vr4),
            adjacencies);

    Prefix6 r4Loopback =
        Prefix6.parse(
            "2001:db8:4::4/128");

    Ospfv3IntraAreaRoute6
        r3ToR4 =
            findIntraAreaRoute(
                vr3,
                r4Loopback);

    assertThat(
        r3ToR4,
        notNullValue());

    /*
     * r3 and r4 are both DROTHER, so r3 must not learn r4
     * directly.
     *
     * Best path is through BDR r1:
     *
     *   r4 loopback metric       1
     * + r1 receive-interface    10
     * + r3 receive-interface    30
     *                           --
     *                           41
     *
     * A direct DROTHER adjacency would incorrectly produce 31.
     */
    assertThat(
        r3ToR4.getMetric(),
        equalTo(41L));

    assertThat(
        r3ToR4.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:10::1")));

    Prefix6 r3Loopback =
        Prefix6.parse(
            "2001:db8:3::3/128");

    Ospfv3IntraAreaRoute6
        r4ToR3 =
            findIntraAreaRoute(
                vr4,
                r3Loopback);

    assertThat(
        r4ToR3,
        notNullValue());

    /*
     * The BDR also relays r3's LSA back onto the same broadcast
     * interface toward r4.
     */
    assertThat(
        r4ToR3.getMetric(),
        equalTo(51L));

    assertThat(
        r4ToR3.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:10::1")));
  }

  @Test
  public void testAllPriorityZeroElectsNoDrOrBdr() {
    Node r1 =
        TestUtils.makeIosRouter("zero1");

    Node r2 =
        TestUtils.makeIosRouter("zero2");

    addLan(
        r1,
        "zero1-lan",
        "2001:db8:20::1/64",
        10,
        0);

    addLan(
        r2,
        "zero2-lan",
        "2001:db8:20::2/64",
        20,
        0);

    addLoopback(
        r1,
        "zero1-loop",
        "2001:db8:101::1/128");

    addLoopback(
        r2,
        "zero2-loop",
        "2001:db8:102::2/128");

    addProcess(
        r1,
        "192.0.2.101",
        "zero1-lan",
        "zero1-loop");

    addProcess(
        r2,
        "192.0.2.102",
        "zero2-lan",
        "zero2-loop");

    VirtualRouter vr1 =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr2 =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder().build();

    vr1.initForIgpComputation(topology);
    vr2.initForIgpComputation(topology);

    NodeInterfacePair r1Lan =
        NodeInterfacePair.of(
            "zero1",
            "zero1-lan");

    NodeInterfacePair r2Lan =
        NodeInterfacePair.of(
            "zero2",
            "zero2-lan");

    BroadcastL3Adjacencies adjacencies =
        new BroadcastL3Adjacencies(
            Set.of(
                r1Lan,
                r2Lan));

    Map<String, Node> nodes =
        ImmutableMap.of(
            "zero1", r1,
            "zero2", r2);

    Ospfv3RoutingProcess.BroadcastElection
        election =
            vr1.getOspfv3Processes()
                .get("1")
                .electBroadcastDesignatedRouters(
                    r1.getConfiguration()
                        .getAllInterfaces()
                        .get("zero1-lan"),
                    r1Lan,
                    nodes,
                    adjacencies);

    assertThat(
        election.getDr(),
        equalTo(null));

    assertThat(
        election.getBdr(),
        equalTo(null));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            List.of(
                vr1,
                vr2),
            adjacencies);

    /*
     * With every interface priority zero there is no DR/BDR,
     * so neither DROTHER can establish a full adjacency.
     */
    assertThat(
        findIntraAreaRoute(
            vr1,
            Prefix6.parse(
                "2001:db8:102::2/128")),
        equalTo(null));

    assertThat(
        findIntraAreaRoute(
            vr2,
            Prefix6.parse(
                "2001:db8:101::1/128")),
        equalTo(null));
  }
  @Test
  public void testDuplicateRouterIdExcludedFromElection() {

    Node r1 =
        TestUtils.makeIosRouter(
            "duplicate-dr1");

    Node r2 =
        TestUtils.makeIosRouter(
            "duplicate-dr2");

    addLan(
        r1,
        "duplicate-dr1-lan",
        "2001:db8:82:20::1/64",
        10,
        10);

    addLan(
        r2,
        "duplicate-dr2-lan",
        "2001:db8:82:20::2/64",
        20,
        200);

    addLoopback(
        r1,
        "duplicate-dr1-loop",
        "2001:db8:82:101::1/128");

    addLoopback(
        r2,
        "duplicate-dr2-loop",
        "2001:db8:82:102::2/128");

    /*
     * r2 would win the election by priority if it were a valid neighbor,
     * but its router ID duplicates r1's router ID.
     */
    addProcess(
        r1,
        "192.0.2.182",
        "duplicate-dr1-lan",
        "duplicate-dr1-loop");

    addProcess(
        r2,
        "192.0.2.182",
        "duplicate-dr2-lan",
        "duplicate-dr2-loop");

    VirtualRouter vr1 =
        r1.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    VirtualRouter vr2 =
        r2.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    TopologyContext topology =
        TopologyContext.builder()
            .build();

    vr1.initForIgpComputation(
        topology);

    vr2.initForIgpComputation(
        topology);

    NodeInterfacePair r1Lan =
        NodeInterfacePair.of(
            "duplicate-dr1",
            "duplicate-dr1-lan");

    NodeInterfacePair r2Lan =
        NodeInterfacePair.of(
            "duplicate-dr2",
            "duplicate-dr2-lan");

    BroadcastL3Adjacencies adjacencies =
        new BroadcastL3Adjacencies(
            Set.of(
                r1Lan,
                r2Lan));

    Map<String, Node> nodes =
        ImmutableMap.of(
            "duplicate-dr1",
            r1,
            "duplicate-dr2",
            r2);

    Ospfv3RoutingProcess.BroadcastElection election =
        vr1.getOspfv3Processes()
            .get("1")
            .electBroadcastDesignatedRouters(
                r1.getConfiguration()
                    .getAllInterfaces()
                    .get(
                        "duplicate-dr1-lan"),
                r1Lan,
                nodes,
                adjacencies);

    /*
     * r2 is not a valid OSPF neighbor candidate because its router ID
     * duplicates ours. r1 is therefore the only eligible candidate.
     */
    assertThat(
        election.getDr(),
        equalTo(
            r1Lan));

    assertThat(
        election.getBdr(),
        equalTo(
            null));

    IncrementalBdpEngine
        .initOspfv3InternalRoutes(
            nodes,
            List.of(
                vr1,
                vr2),
            adjacencies);

    /*
     * The duplicate-ID pair must not exchange routes on the broadcast
     * network either.
     */
    assertThat(
        findIntraAreaRoute(
            vr1,
            Prefix6.parse(
                "2001:db8:82:102::2/128")),
        equalTo(
            null));

    assertThat(
        findIntraAreaRoute(
            vr2,
            Prefix6.parse(
                "2001:db8:82:101::1/128")),
        equalTo(
            null));
  }

}
