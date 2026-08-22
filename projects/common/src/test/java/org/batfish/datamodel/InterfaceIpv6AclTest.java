package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

/** Tests IPv6 ACL attachment to interfaces. */
public final class InterfaceIpv6AclTest {

  @Test
  public void testIpv6FiltersAttached() {
    Configuration c =
        Configuration.builder()
            .setHostname("n1")
            .setConfigurationFormat(
                ConfigurationFormat.CISCO_IOS)
            .build();

    Vrf vrf =
        Vrf.builder()
            .setName(
                Configuration.DEFAULT_VRF_NAME)
            .setOwner(c)
            .build();

    Ip6AccessList incoming =
        Ip6AccessList.builder()
            .setName("V6-IN")
            .setLines(
                Ip6AccessListLine.builder()
                    .setAction(LineAction.PERMIT)
                    .build())
            .build();

    Ip6AccessList outgoing =
        Ip6AccessList.builder()
            .setName("V6-OUT")
            .setLines(
                Ip6AccessListLine.builder()
                    .setAction(LineAction.DENY)
                    .build())
            .build();

    c.getIp6AccessLists()
        .put(incoming.getName(), incoming);
    c.getIp6AccessLists()
        .put(outgoing.getName(), outgoing);

    Interface iface =
        Interface.builder()
            .setName("Ethernet1")
            .setOwner(c)
            .setVrf(vrf)
            .setType(InterfaceType.PHYSICAL)
            .setIncomingFilter6(incoming)
            .setOutgoingFilter6(outgoing)
            .build();

    assertThat(
        iface.getIncomingFilter6(),
        sameInstance(incoming));

    assertThat(
        iface.getOutgoingFilter6(),
        sameInstance(outgoing));

    assertThat(
        iface.getIncomingFilter6().getName(),
        equalTo("V6-IN"));

    assertThat(
        iface.getOutgoingFilter6().getName(),
        equalTo("V6-OUT"));
  }
}
