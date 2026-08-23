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
}
