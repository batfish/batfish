package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.AwsLocationInfoUtils.subnetInterfaceLinkLocationInfo;
import static org.batfish.representation.aws.AwsLocationInfoUtils.subnetInterfaceLocationInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TestInterface;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

/** Test for {@link AwsLocationInfoUtils}. */
public class AwsLocationInfoUtilsTest {
  private static final Interface IFACE;

  static {
    IFACE = TestInterface.builder().setName("i").build();
    IFACE.setAllAddresses(
        ImmutableList.of(
            ConcreteInterfaceAddress.parse("1.1.1.1/24"),
            ConcreteInterfaceAddress.parse("2.2.2.2/24")));
  }

  @Test
  public void testInstanceInterfaceLocationInfo() {
    LocationInfo info = instanceInterfaceLocationInfo(IFACE);
    assertTrue(info.isSource());
    assertThat(
        info.getSourceIps(),
        allOf(
            containsIp(Ip.parse("1.1.1.1")),
            containsIp(Ip.parse("2.2.2.2")),
            not(containsIp(Ip.parse("1.1.1.2"))),
            not(containsIp(Ip.parse("2.2.2.3")))));
    assertThat(info.getArpIps(), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSubnetInterfaceLocationInfo() {
    LocationInfo info = subnetInterfaceLocationInfo(IFACE);
    assertFalse(info.isSource());
    assertThat(
        info.getSourceIps(),
        allOf(
            containsIp(Ip.parse("1.1.1.1")),
            containsIp(Ip.parse("2.2.2.2")),
            not(containsIp(Ip.parse("1.1.1.2"))),
            not(containsIp(Ip.parse("2.2.2.3")))));
    assertThat(info.getArpIps(), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSubnetInterfaceLinkLocationInfo() {
    LocationInfo info = subnetInterfaceLinkLocationInfo(IFACE);
    assertFalse(info.isSource());
    assertThat(
        info.getSourceIps(),
        allOf(containsIp(Ip.parse("1.1.1.2")), containsIp(Ip.parse("2.2.2.3"))));
    assertThat(info.getArpIps(), equalTo(EmptyIpSpace.INSTANCE));
  }
}
