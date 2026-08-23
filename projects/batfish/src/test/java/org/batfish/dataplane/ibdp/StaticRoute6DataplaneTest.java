package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.StaticRoute6;
import org.junit.Test;

/** Tests IPv6 static-route activation. */
public final class StaticRoute6DataplaneTest {

  @Test
  public void testStaticActivation() {
    Node node =
        TestUtils.makeIosRouter("n1");

    Configuration c =
        node.getConfiguration();

    Interface.builder()
        .setName("Ethernet1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                "2001:db8:10::1/64"))
        .build();

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:100::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:10::2"))
                .build());

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:200::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:ffff::2"))
                .build());

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:300::/64"))
                .setNextHopInterface(
                    Interface.NULL_INTERFACE_NAME)
                .build());

    VirtualRouter vr =
        node.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    vr.initForIgpComputation(
        TopologyContext.builder().build());

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:100::/64")),
        hasSize(1));

    AbstractRoute6 resolved =
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:100::/64"))
            .iterator()
            .next();

    assertThat(
        resolved.getNextHopInterface(),
        equalTo("Ethernet1"));

    assertThat(
        resolved.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:10::2")));

    // Unresolved static next hop must remain inactive.
    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:200::/64")),
        hasSize(0));

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:300::/64")),
        hasSize(1));
  }
  @Test
  public void testRecursiveStaticChainAndLoopSuppression() {
    Node node =
        TestUtils.makeIosRouter("n1");

    Configuration c =
        node.getConfiguration();

    Interface.builder()
        .setName("Ethernet1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                "2001:db8:10::1/64"))
        .build();

    /*
     * Deliberately add the dependent prefix before its resolver prefix.
     * The resolver therefore requires more than one fixed-point pass.
     *
     * 100::/64 -> 200::2
     * 200::/64 -> 10::2 -> Ethernet1
     */
    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:100::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:200::2"))
                .build());

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:200::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:10::2"))
                .build());

    /*
     * Circular recursion must never install either route.
     *
     * 300::/64 -> 400::2
     * 400::/64 -> 300::2
     */
    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:300::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:400::2"))
                .build());

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(
            StaticRoute6.builder()
                .setNetwork(
                    Prefix6.parse(
                        "2001:db8:400::/64"))
                .setNextHopIp(
                    Ip6.parse(
                        "2001:db8:300::2"))
                .build());

    VirtualRouter vr =
        node.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    vr.initForIgpComputation(
        TopologyContext.builder().build());

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:100::/64")),
        hasSize(1));

    AbstractRoute6 recursive =
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:100::/64"))
            .iterator()
            .next();

    assertThat(
        recursive.getNextHopInterface(),
        equalTo("Ethernet1"));

    /*
     * 200::2 is not on-link. The final immediate neighbor must be inherited
     * from the resolver route instead.
     */
    assertThat(
        recursive.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:10::2")));

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:300::/64")),
        hasSize(0));

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                Prefix6.parse(
                    "2001:db8:400::/64")),
        hasSize(0));
  }

  @Test
  public void testRecursiveStaticTracksOspfv3Resolution() {
    Node node =
        TestUtils.makeIosRouter("n1");

    Configuration c =
        node.getConfiguration();

    Interface.builder()
        .setName("Ethernet1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                "2001:db8:10::1/64"))
        .build();

    StaticRoute6 recursiveStatic =
        StaticRoute6.builder()
            .setNetwork(
                Prefix6.parse(
                    "2001:db8:100::/64"))
            .setNextHopIp(
                Ip6.parse(
                    "2001:db8:200::2"))
            .build();

    c.getDefaultVrf()
        .getStaticRoutes6()
        .add(recursiveStatic);

    VirtualRouter vr =
        node.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    vr.initForIgpComputation(
        TopologyContext.builder().build());

    // No route to the recursive next hop yet.
    assertThat(
        vr.getMainRib6()
            .getRoutes(
                recursiveStatic.getNetwork()),
        hasSize(0));

    Ospfv3IntraAreaRoute6 resolver =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse(
                "2001:db8:200::/64"),
            "Ethernet1",
            Ip6.parse(
                "2001:db8:10::2"),
            110,
            10,
            0L);

    vr.getMainRib6()
        .mergeRoute(resolver);

    vr.refreshStaticRoutes6();

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                recursiveStatic.getNetwork()),
        hasSize(1));

    AbstractRoute6 resolvedStatic =
        vr.getMainRib6()
            .getRoutes(
                recursiveStatic.getNetwork())
            .iterator()
            .next();

    assertThat(
        resolvedStatic.getNextHopInterface(),
        equalTo("Ethernet1"));

    assertThat(
        resolvedStatic.getNextHopIp(),
        equalTo(
            Ip6.parse(
                "2001:db8:10::2")));

    /*
     * Withdrawal of the resolver must also withdraw the dependent static.
     */
    vr.getMainRib6()
        .removeRoute(resolver);

    vr.refreshStaticRoutes6();

    assertThat(
        vr.getMainRib6()
            .getRoutes(
                recursiveStatic.getNetwork()),
        hasSize(0));
  }

}
