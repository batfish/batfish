package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpWildcard;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

public class CheckPointGatewayConversionsTest {

  @Test
  public void testToIpSpace_addressRange() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    AddressRange range = new AddressRange(ip1, ip2, null, null, "name", Uid.of("uid"));
    assertThat(toIpSpace(range), equalTo(IpRange.range(ip1, ip2)));
  }

  @Test
  public void testToIpSpace_addressRangeIpv6() {
    Ip6 ip1 = Ip6.parse("1::1");
    Ip6 ip2 = Ip6.parse("1::2");
    AddressRange range = new AddressRange(null, null, ip1, ip2, "name", Uid.of("uid"));
    assertNull(toIpSpace(range));
  }

  @Test
  public void testToIpSpace_network() {
    Ip ip = Ip.parse("1.1.1.0");
    Ip mask = Ip.parse("255.255.255.0");
    Network network = new Network("name", ip, mask, Uid.of("uid"));
    Ip flippedMask = Ip.parse("0.0.0.255");
    assertThat(
        toIpSpace(network), equalTo(IpWildcard.ipWithWildcardMask(ip, flippedMask).toIpSpace()));
  }

  @Test
  public void testToIpAccessLists() {
    // TODO test a section and rule
  }

  @Test
  public void testToAclLine() {
    ImmutableMap<Uid, TypedManagementObject> objs =
        ImmutableMap.of(
            Uid.of("10"),
            new Network("net0", Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"), Uid.of("10")),
            Uid.of("11"),
            new Network("net1", Ip.parse("10.0.1.0"), Ip.parse("255.255.255.0"), Uid.of("11")),
            Uid.of("12"),
            new Network("net2", Ip.parse("10.0.2.0"), Ip.parse("255.255.255.0"), Uid.of("12")),
            Uid.of("13"),
            new Network("net3", Ip.parse("10.0.3.0"), Ip.parse("255.255.255.0"), Uid.of("13")));
  }
}
