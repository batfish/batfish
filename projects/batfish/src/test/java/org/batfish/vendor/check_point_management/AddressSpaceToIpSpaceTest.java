package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

/** Tests of {@link AddressSpaceToIpSpace}. */
public class AddressSpaceToIpSpaceTest {
  @Test
  public void testAddressRange() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    AddressRange range = new AddressRange(ip1, ip2, null, null, "name", Uid.of("uid"));
    assertThat(range.accept(visitor), equalTo(IpRange.range(ip1, ip2)));
  }

  @Test
  public void testAddressRangeIpv6() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    Ip6 ip1 = Ip6.parse("1::1");
    Ip6 ip2 = Ip6.parse("1::2");
    AddressRange range = new AddressRange(null, null, ip1, ip2, "name", Uid.of("uid"));
    assertThat(range.accept(visitor), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testCpmiAnyObject() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    CpmiAnyObject cpmiAnyObject = new CpmiAnyObject(Uid.of("1"));
    assertThat(cpmiAnyObject.accept(visitor), equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testGatewayOrServer() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    GatewayOrServer gatewayOrServer =
        new SimpleGateway(
            Ip.parse("10.0.0.1"),
            "simpleGateway",
            ImmutableList.of(
                new Interface(
                    "eth1", InterfaceTopologyTest.TEST_INSTANCE, Ip.parse("10.0.1.1"), 24),
                new Interface(
                    "eth2", InterfaceTopologyTest.TEST_INSTANCE, Ip.parse("10.0.2.1"), 24)),
            new GatewayOrServerPolicy(null, null),
            Uid.of("1"));
    assertThat(
        gatewayOrServer.accept(visitor),
        equalTo(
            AclIpSpace.union(Ip.parse("10.0.1.1").toIpSpace(), Ip.parse("10.0.2.1").toIpSpace())));
  }

  @Test
  public void testGroup() {
    Uid group1Uid = Uid.of("1");
    Uid group2Uid = Uid.of("2");
    Uid group3Uid = Uid.of("3");
    Uid host1Uid = Uid.of("11");
    Uid host2Uid = Uid.of("12");
    Uid host3Uid = Uid.of("13");

    Ip hostIp1 = Ip.parse("10.10.10.11");
    Ip hostIp2 = Ip.parse("10.10.10.12");
    Ip hostIp3 = Ip.parse("10.10.10.13");

    Group group1 = new Group("group1", ImmutableList.of(group2Uid, host1Uid), group1Uid);
    Group group2 = new Group("group2", ImmutableList.of(group1Uid, group3Uid, host2Uid), group2Uid);
    Group group3 = new Group("group3", ImmutableList.of(host3Uid), group3Uid);
    Host host1 = new Host(hostIp1, "host1", host1Uid);
    Host host2 = new Host(hostIp2, "host2", host2Uid);
    Host host3 = new Host(hostIp3, "host3", host3Uid);

    AddressSpaceToIpSpace visitor =
        new AddressSpaceToIpSpace(
            ImmutableMap.<Uid, TypedManagementObject>builder()
                .put(group1Uid, group1)
                .put(group2Uid, group2)
                .put(group3Uid, group3)
                .put(host1Uid, host1)
                .put(host2Uid, host2)
                .put(host3Uid, host3)
                .build());
    assertThat(
        group1.accept(visitor),
        equalTo(
            AclIpSpace.union(
                Ip.parse("10.10.10.11").toIpSpace(),
                Ip.parse("10.10.10.12").toIpSpace(),
                Ip.parse("10.10.10.13").toIpSpace())));
  }

  @Test
  public void testHost() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    Ip hostIp = Ip.parse("10.10.10.10");
    Host host = new Host(hostIp, "hostName", Uid.of("10"));
    assertThat(host.accept(visitor), equalTo(hostIp.toIpSpace()));
  }

  @Test
  public void testNetwork() {
    AddressSpaceToIpSpace visitor = new AddressSpaceToIpSpace(ImmutableMap.of());
    Ip ip = Ip.parse("1.1.1.0");
    Ip mask = Ip.parse("255.255.255.0");
    Network network = new Network("name", ip, mask, Uid.of("uid"));
    Ip flippedMask = Ip.parse("0.0.0.255");
    assertThat(
        network.accept(visitor),
        equalTo(IpWildcard.ipWithWildcardMask(ip, flippedMask).toIpSpace()));
  }
}
