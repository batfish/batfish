package org.batfish.vendor.check_point_management;

import static org.batfish.vendor.check_point_management.AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpaceMetadata;
import org.junit.Test;

/** Tests of {@link AddressSpaceToIpSpaceMetadata}. */
public class AddressSpaceToIpSpaceMetadataTest {

  @Test
  public void testAddressRange() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    AddressRange addrSpace =
        new AddressRange(
            ip1, ip2, null, null, NatSettingsTest.TEST_INSTANCE, "name", Uid.of("uid"));
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("address-range"));
  }

  @Test
  public void testCpmiAnyObject() {
    CpmiAnyObject addrSpace = new CpmiAnyObject(Uid.of("1"));
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("network object"));
  }

  @Test
  public void testGatewayOrServer() {
    GatewayOrServer addrSpace =
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
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("gateway or server"));
  }

  @Test
  public void testGroup() {
    Uid group1Uid = Uid.of("1");
    Group addrSpace = new Group("group1", ImmutableList.of(), group1Uid);
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("group"));
  }

  @Test
  public void testHost() {
    Ip hostIp = Ip.parse("10.10.10.10");
    Host addrSpace = new Host(hostIp, NatSettingsTest.TEST_INSTANCE, "hostName", Uid.of("10"));
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("host"));
  }

  @Test
  public void testNetwork() {
    Ip ip = Ip.parse("1.1.1.0");
    Ip mask = Ip.parse("255.255.255.0");
    Network addrSpace = new Network("name", NatSettingsTest.TEST_INSTANCE, ip, mask, Uid.of("uid"));
    IpSpaceMetadata metadata = toIpSpaceMetadata(addrSpace);
    assertThat(metadata.getSourceName(), equalTo(addrSpace.getName()));
    assertThat(metadata.getSourceType(), equalTo("network"));
  }
}
