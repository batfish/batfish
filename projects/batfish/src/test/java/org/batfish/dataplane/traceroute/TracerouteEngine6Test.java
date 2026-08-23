package org.batfish.dataplane.traceroute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Fib6;
import org.batfish.datamodel.Fib6Impl;
import org.batfish.datamodel.Flow6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.FinalMainRib6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Tests route/FIB-level IPv6 path tracing. */
public final class TracerouteEngine6Test {

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

  private static Configuration configuration(
      String hostname) {
    Configuration c =
        new Configuration(
            hostname,
            ConfigurationFormat.CISCO_IOS);
    c.getVrfs()
        .put(
            Configuration.DEFAULT_VRF_NAME,
            org.batfish.datamodel.Vrf.builder()
                .setName(
                    Configuration.DEFAULT_VRF_NAME)
                .setOwner(c)
                .build());
    return c;
  }

  private static void addInterface(
      Configuration c,
      String name,
      String address,
      InterfaceType type) {
    Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(type)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                address))
        .build();
  }

  private static Fib6 fib(
      org.batfish.datamodel.AbstractRoute6...
          routes) {
    return new Fib6Impl(
        FinalMainRib6.of(routes));
  }

  @Test
  public void testThreeNodeAcceptedPath() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");
    Configuration n3 =
        configuration("n3");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth23",
        "2001:db8:23::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n3,
        "eth32",
        "2001:db8:23::3/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n3,
        "loopback0",
        "2001:db8:3::3/128",
        InterfaceType.LOOPBACK);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:3::3/128");

    Ospfv3IntraAreaRoute6 n1Route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            20,
            0L);

    Ospfv3IntraAreaRoute6 n2Route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth23",
            Ip6.parse(
                "2001:db8:23::3"),
            110,
            10,
            0L);

    ConnectedRoute6 n3Route =
        new ConnectedRoute6(
            destination,
            "loopback0");

    Map<String, Configuration> configs =
        ImmutableMap.of(
            "n1", n1,
            "n2", n2,
            "n3", n3);

    Map<String, Map<String, Fib6>> fibs =
        ImmutableMap.of(
            "n1",
            ImmutableMap.of(
                Configuration.DEFAULT_VRF_NAME,
                fib(n1Route)),
            "n2",
            ImmutableMap.of(
                Configuration.DEFAULT_VRF_NAME,
                fib(n2Route)),
            "n3",
            ImmutableMap.of(
                Configuration.DEFAULT_VRF_NAME,
                fib(n3Route)));

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            configs,
            fibs);

    List<Ipv6Trace> traces =
        engine.computeTraces(
            "n1",
            Configuration.DEFAULT_VRF_NAME,
            Ip6.parse(
                "2001:db8:3::3"));

    assertThat(
        traces,
        hasSize(1));

    Ipv6Trace trace =
        traces.get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition.ACCEPTED));

    assertThat(
        trace.getHops()
            .stream()
            .map(Ipv6TraceHop::getNode)
            .toList(),
        contains(
            "n1",
            "n2",
            "n3"));

    assertThat(
        trace.getHops()
            .get(0)
            .getOutgoingInterface()
            .orElseThrow(),
        equalTo("eth12"));

    assertThat(
        trace.getHops()
            .get(0)
            .getNdTarget()
            .orElseThrow(),
        equalTo(
            Ip6.parse(
                "2001:db8:12::2")));

    assertThat(
        trace.getHops()
            .get(1)
            .getNdTarget()
            .orElseThrow(),
        equalTo(
            Ip6.parse(
                "2001:db8:23::3")));
  }

  @Test
  public void testNoRoute() {
    Configuration n1 =
        configuration("n1");

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1),
            ImmutableMap.of());

    Ipv6Trace trace =
        engine.computeTraces(
                "n1",
                Configuration.DEFAULT_VRF_NAME,
                Ip6.parse(
                    "2001:db8:ffff::1"))
            .get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition.NO_ROUTE));
  }

  @Test
  public void testExplicitNeighborUnreachable() {
    Configuration n1 =
        configuration("n1");

    addInterface(
        n1,
        "eth1",
        "2001:db8:1::1/64",
        InterfaceType.PHYSICAL);

    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse(
                "2001:db8:99::/64"),
            "eth1",
            Ip6.parse(
                "2001:db8:1::2"),
            110,
            10,
            0L);

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route))));

    Ipv6Trace trace =
        engine.computeTraces(
                "n1",
                Configuration.DEFAULT_VRF_NAME,
                Ip6.parse(
                    "2001:db8:99::1"))
            .get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition
                .NEIGHBOR_UNREACHABLE));
  }

  @Test
  public void testConnectedDestinationExitsModeledNetwork() {
    Configuration n1 =
        configuration("n1");

    addInterface(
        n1,
        "eth1",
        "2001:db8:1::1/64",
        InterfaceType.PHYSICAL);

    ConnectedRoute6 connected =
        new ConnectedRoute6(
            Prefix6.parse(
                "2001:db8:1::/64"),
            "eth1");

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(connected))));

    Ipv6Trace trace =
        engine.computeTraces(
                "n1",
                Configuration.DEFAULT_VRF_NAME,
                Ip6.parse(
                    "2001:db8:1::100"))
            .get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition
                .EXITS_NETWORK));

    assertThat(
        trace.getHops()
            .get(0)
            .getNdTarget()
            .orElseThrow(),
        equalTo(
            Ip6.parse(
                "2001:db8:1::100")));
  }

  @Test
  public void testEcmpProducesMultipleTraces() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");
    Configuration n4 =
        configuration("n4");
    Configuration dst =
        configuration("dst");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL);
    addInterface(
        n1,
        "eth14",
        "2001:db8:14::1/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL);
    addInterface(
        n2,
        "eth2d",
        "2001:db8:2d::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n4,
        "eth41",
        "2001:db8:14::4/64",
        InterfaceType.PHYSICAL);
    addInterface(
        n4,
        "eth4d",
        "2001:db8:4d::4/64",
        InterfaceType.PHYSICAL);

    addInterface(
        dst,
        "ethd2",
        "2001:db8:2d::d/64",
        InterfaceType.PHYSICAL);
    addInterface(
        dst,
        "ethd4",
        "2001:db8:4d::d/64",
        InterfaceType.PHYSICAL);
    addInterface(
        dst,
        "loopback0",
        "2001:db8:dead::1/128",
        InterfaceType.LOOPBACK);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:dead::1/128");

    Ospfv3IntraAreaRoute6 viaN2 =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            20,
            0L);

    Ospfv3IntraAreaRoute6 viaN4 =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth14",
            Ip6.parse(
                "2001:db8:14::4"),
            110,
            20,
            0L);

    Ospfv3IntraAreaRoute6 n2Route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth2d",
            Ip6.parse(
                "2001:db8:2d::d"),
            110,
            10,
            0L);

    Ospfv3IntraAreaRoute6 n4Route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth4d",
            Ip6.parse(
                "2001:db8:4d::d"),
            110,
            10,
            0L);

    ConnectedRoute6 dstRoute =
        new ConnectedRoute6(
            destination,
            "loopback0");

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2,
                "n4", n4,
                "dst", dst),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(viaN2, viaN4)),
                "n2",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(n2Route)),
                "n4",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(n4Route)),
                "dst",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(dstRoute))));

    List<Ipv6Trace> traces =
        engine.computeTraces(
            "n1",
            Configuration.DEFAULT_VRF_NAME,
            Ip6.parse(
                "2001:db8:dead::1"));

    assertThat(
        traces,
        hasSize(2));

    assertThat(
        traces.stream()
            .allMatch(
                trace ->
                    trace.getDisposition()
                        == Ipv6TraceDisposition.ACCEPTED),
        equalTo(true));
  }
  @Test
  public void testLinkLocalOnlyPointToPointForwarding() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");

    /*
     * Deliberately give the transit interfaces no concrete global IPv6
     * addresses. This represents an OSPFv3 link configured with only
     * automatically generated link-local addressing.
     */
    Interface.builder()
        .setName("eth12")
        .setOwner(n1)
        .setVrf(n1.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .build();

    Interface.builder()
        .setName("eth21")
        .setOwner(n2)
        .setVrf(n2.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .build();

    addInterface(
        n2,
        "loopback0",
        "2001:db8:2::2/128",
        InterfaceType.LOOPBACK);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:2::2/128");

    /*
     * No next-hop Ip6 is supplied. The OSPFv3 dataplane uses this form when
     * the physical peer is known but its generated link-local address is not.
     */
    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            null,
            110,
            10,
            0L);

    TestL3Adjacencies adjacencies =
        new TestL3Adjacencies();

    adjacencies.addPair(
        NodeInterfacePair.of(
            "n1", "eth12"),
        NodeInterfacePair.of(
            "n2", "eth21"));

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route)),
                "n2",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(
                        new ConnectedRoute6(
                            destination,
                            "loopback0")))),
            adjacencies);

    Ipv6Trace trace =
        engine.computeTraces(
                "n1",
                Configuration.DEFAULT_VRF_NAME,
                Ip6.parse(
                    "2001:db8:2::2"))
            .get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition.ACCEPTED));

    assertThat(
        trace.getHops()
            .stream()
            .map(Ipv6TraceHop::getNode)
            .toList(),
        contains("n1", "n2"));

    /*
     * We know which physical peer receives the packet, but we intentionally
     * do not invent the peer's automatically generated fe80:: address.
     */
    assertThat(
        trace.getHops()
            .get(0)
            .getNdTarget()
            .isEmpty(),
        equalTo(true));
  }

  @Test
  public void testLinkLocalOnlyRouteWithoutTopologyIsUnreachable() {
    Configuration n1 =
        configuration("n1");

    Interface.builder()
        .setName("eth1")
        .setOwner(n1)
        .setVrf(n1.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .build();

    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse(
                "2001:db8:99::/64"),
            "eth1",
            null,
            110,
            10,
            0L);

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route))));

    Ipv6Trace trace =
        engine.computeTraces(
                "n1",
                Configuration.DEFAULT_VRF_NAME,
                Ip6.parse(
                    "2001:db8:99::1"))
            .get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition
                .NEIGHBOR_UNREACHABLE));
  }

  @Test
  public void testFlow6Api() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "loopback0",
        "2001:db8:2::2/128",
        InterfaceType.LOOPBACK);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:2::2/128");

    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            10,
            0L);

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route)),
                "n2",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(
                        new ConnectedRoute6(
                            destination,
                            "loopback0")))));

    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setSrcIp(
                Ip6.parse(
                    "2001:db8:1::10"))
            .setDstIp(
                Ip6.parse(
                    "2001:db8:2::2"))
            .setIpProtocol(
                IpProtocol.TCP)
            .setSrcPort(49152)
            .setDstPort(443)
            .setPacketLength(80)
            .build();

    List<Ipv6Trace> traces =
        engine.computeTraces(flow);

    assertThat(
        traces,
        hasSize(1));

    assertThat(
        traces.get(0)
            .getDisposition(),
        equalTo(
            Ipv6TraceDisposition.ACCEPTED));

    assertThat(
        traces.get(0)
            .getHops()
            .stream()
            .map(Ipv6TraceHop::getNode)
            .toList(),
        contains("n1", "n2"));
  }

  @Test
  public void testFlow6DeniedOutgoing() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "loopback0",
        "2001:db8:2::2/128",
        InterfaceType.LOOPBACK);

    org.batfish.datamodel.Ip6AccessList blockHttps =
        org.batfish.datamodel.Ip6AccessList.builder()
            .setName("BLOCK-HTTPS")
            .setLines(
                org.batfish.datamodel.Ip6AccessListLine.builder()
                    .setAction(
                        org.batfish.datamodel.LineAction.DENY)
                    .setProtocol(IpProtocol.TCP)
                    .setDstPorts(
                        org.batfish.datamodel.SubRange.singleton(
                            443))
                    .build(),
                org.batfish.datamodel.Ip6AccessListLine.builder()
                    .setAction(
                        org.batfish.datamodel.LineAction.PERMIT)
                    .build())
            .build();

    n1.getIp6AccessLists()
        .put(
            blockHttps.getName(),
            blockHttps);

    n1.getAllInterfaces()
        .get("eth12")
        .setOutgoingFilter6(
            blockHttps);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:2::2/128");

    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            10,
            0L);

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route)),
                "n2",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(
                        new ConnectedRoute6(
                            destination,
                            "loopback0")))));

    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setSrcIp(
                Ip6.parse(
                    "2001:db8:1::10"))
            .setDstIp(
                Ip6.parse(
                    "2001:db8:2::2"))
            .setIpProtocol(
                IpProtocol.TCP)
            .setSrcPort(40000)
            .setDstPort(443)
            .setPacketLength(80)
            .build();

    Ipv6Trace trace =
        engine.computeTraces(flow).get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition
                .DENIED_OUT));
  }

  @Test
  public void testFlow6DeniedIncoming() {
    Configuration n1 =
        configuration("n1");
    Configuration n2 =
        configuration("n2");

    addInterface(
        n1,
        "eth12",
        "2001:db8:12::1/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "eth21",
        "2001:db8:12::2/64",
        InterfaceType.PHYSICAL);

    addInterface(
        n2,
        "loopback0",
        "2001:db8:2::2/128",
        InterfaceType.LOOPBACK);

    org.batfish.datamodel.Ip6AccessList blockSource =
        org.batfish.datamodel.Ip6AccessList.builder()
            .setName("BLOCK-SOURCE")
            .setLines(
                org.batfish.datamodel.Ip6AccessListLine.builder()
                    .setAction(
                        org.batfish.datamodel.LineAction.DENY)
                    .setSrcPrefix(
                        Prefix6.parse(
                            "2001:db8:1::/64"))
                    .build(),
                org.batfish.datamodel.Ip6AccessListLine.builder()
                    .setAction(
                        org.batfish.datamodel.LineAction.PERMIT)
                    .build())
            .build();

    n2.getIp6AccessLists()
        .put(
            blockSource.getName(),
            blockSource);

    n2.getAllInterfaces()
        .get("eth21")
        .setIncomingFilter6(
            blockSource);

    Prefix6 destination =
        Prefix6.parse(
            "2001:db8:2::2/128");

    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            destination,
            "eth12",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            10,
            0L);

    TracerouteEngine6 engine =
        new TracerouteEngine6(
            ImmutableMap.of(
                "n1", n1,
                "n2", n2),
            ImmutableMap.of(
                "n1",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(route)),
                "n2",
                ImmutableMap.of(
                    Configuration.DEFAULT_VRF_NAME,
                    fib(
                        new ConnectedRoute6(
                            destination,
                            "loopback0")))));

    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setSrcIp(
                Ip6.parse(
                    "2001:db8:1::10"))
            .setDstIp(
                Ip6.parse(
                    "2001:db8:2::2"))
            .setIpProtocol(
                IpProtocol.TCP)
            .setSrcPort(40000)
            .setDstPort(443)
            .setPacketLength(80)
            .build();

    Ipv6Trace trace =
        engine.computeTraces(flow).get(0);

    assertThat(
        trace.getDisposition(),
        equalTo(
            Ipv6TraceDisposition
                .DENIED_IN));
  }


}
