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
            Ip6.parse("2001:db8:12::1"),
            110,
            25,
            10L,
            30L,
            Ip.parse("192.0.2.1"));

    Ospfv3ExternalType2Route6 clone =
        BatfishObjectMapper.clone(
            route, Ospfv3ExternalType2Route6.class);

    assertThat(clone, equalTo(route));
    assertThat(clone.getMetric(), equalTo(25L));
    assertThat(
        clone.getAdvertiser(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(clone.getArea(), equalTo(10L));
    assertThat(
        clone.getCostToAdvertiser(),
        equalTo(30L));
    assertThat(
        clone.getNextHopIp(),
        equalTo(Ip6.parse("2001:db8:12::1")));
  }
  @Test
  public void testInterAreaSerialization() {
    Ospfv3InterAreaRoute6 route =
        new Ospfv3InterAreaRoute6(
            Prefix6.parse("2001:db8:30::/64"),
            "Ethernet3",
            Ip6.parse("2001:db8:23::2"),
            110,
            40,
            2L);

    Ospfv3InterAreaRoute6 clone =
        BatfishObjectMapper.clone(
            route,
            Ospfv3InterAreaRoute6.class);

    assertThat(clone, equalTo(route));
    assertThat(clone.getArea(), equalTo(2L));
    assertThat(clone.getMetric(), equalTo(40L));
    assertThat(
        clone.getNextHopIp(),
        equalTo(
            Ip6.parse("2001:db8:23::2")));
    assertThat(
        clone.getProtocol(),
        equalTo(RoutingProtocol.OSPF3));
  }

  @Test
  public void testNssaExternalSerialization() {
    Ospfv3NssaExternalType2Route6 route =
        new Ospfv3NssaExternalType2Route6(
            Prefix6.parse(
                "2001:db8:7::/64"),
            "Ethernet7",
            Ip6.parse(
                "2001:db8:17::1"),
            113,
            25L,
            7L,
            30L,
            Ip.parse("192.0.2.7"),
            77L);

    Ospfv3NssaExternalType2Route6 clone =
        BatfishObjectMapper.clone(
            route,
            Ospfv3NssaExternalType2Route6.class);

    assertThat(clone, equalTo(route));
    assertThat(
        clone.getProtocol(),
        equalTo(RoutingProtocol.OSPF3));
    assertThat(
        clone.getArea(),
        equalTo(7L));
    assertThat(
        clone.getMetric(),
        equalTo(25L));
    assertThat(
        clone.getCostToAdvertiser(),
        equalTo(30L));
    assertThat(
        clone.getAdvertiser(),
        equalTo(
            Ip.parse("192.0.2.7")));
    assertThat(
        clone.getTag(),
        equalTo(77L));
  }

}
