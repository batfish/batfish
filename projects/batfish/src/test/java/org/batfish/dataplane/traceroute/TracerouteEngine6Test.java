package org.batfish.dataplane.traceroute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Fib6;
import org.batfish.datamodel.Fib6Impl;
import org.batfish.datamodel.FinalMainRib6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

/** Tests route/FIB-level IPv6 path tracing. */
public final class TracerouteEngine6Test {

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
}
