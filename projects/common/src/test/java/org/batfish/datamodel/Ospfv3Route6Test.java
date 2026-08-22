package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for IPv6 OSPFv3 route models. */
public final class Ospfv3Route6Test {

  @Test
  public void testIntraAreaSerialization() {
    Ospfv3IntraAreaRoute6 route =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse("2001:db8:1::/64"),
            "Ethernet1",
            Ip6.parse("2001:db8:1::1"),
            110,
            10,
            0L);

    Ospfv3IntraAreaRoute6 clone =
        BatfishObjectMapper.clone(
            route, Ospfv3IntraAreaRoute6.class);

    assertThat(clone, equalTo(route));
    assertThat(
        clone.getProtocol(),
        equalTo(RoutingProtocol.OSPF3));
  }

  @Test
  public void testExternalSerialization() {
    Ospfv3ExternalType2Route6 route =
        new Ospfv3ExternalType2Route6(
            Prefix6.parse("2001:db8:2::/64"),
            "Ethernet2",
            110,
            25,
            Ip.parse("192.0.2.1"));

    Ospfv3ExternalType2Route6 clone =
        BatfishObjectMapper.clone(
            route, Ospfv3ExternalType2Route6.class);

    assertThat(clone, equalTo(route));
    assertThat(clone.getMetric(), equalTo(25L));
    assertThat(
        clone.getAdvertiser(),
        equalTo(Ip.parse("192.0.2.1")));
  }
}
