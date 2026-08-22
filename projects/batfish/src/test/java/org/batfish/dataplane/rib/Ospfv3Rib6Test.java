package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

/** Tests OSPFv3 route preference. */
public final class Ospfv3Rib6Test {

  @Test
  public void testIntraAreaPreferredToExternal() {
    Prefix6 prefix =
        Prefix6.parse("2001:db8:10::/64");

    Ospfv3ExternalType2Route6 external =
        new Ospfv3ExternalType2Route6(
            prefix,
            "Ethernet2",
            110,
            1,
            Ip.parse("192.0.2.2"));

    Ospfv3IntraAreaRoute6 intra =
        new Ospfv3IntraAreaRoute6(
            prefix,
            "Ethernet1",
            Ip6.parse("2001:db8:10::1"),
            110,
            100,
            0L);

    Ospfv3Rib6 rib = new Ospfv3Rib6();

    assertThat(rib.mergeRoute(external), equalTo(true));
    assertThat(rib.mergeRoute(intra), equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(intra)));

    assertThat(
        rib.getBackupRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(external)));
  }
  @Test
  public void testExternalType2CostToAdvertiserTieBreak() {
    Prefix6 prefix =
        Prefix6.parse("2001:db8:20::/64");

    Ospfv3ExternalType2Route6 farther =
        new Ospfv3ExternalType2Route6(
            prefix,
            "Ethernet2",
            Ip6.parse("2001:db8:12::2"),
            110,
            25,
            0L,
            50L,
            Ip.parse("192.0.2.1"));

    Ospfv3ExternalType2Route6 closer =
        new Ospfv3ExternalType2Route6(
            prefix,
            "Ethernet1",
            Ip6.parse("2001:db8:13::1"),
            110,
            25,
            0L,
            10L,
            Ip.parse("192.0.2.1"));

    Ospfv3Rib6 rib = new Ospfv3Rib6();

    assertThat(
        rib.mergeRoute(farther),
        equalTo(true));
    assertThat(
        rib.mergeRoute(closer),
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                closer)));

    assertThat(
        rib.getBackupRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                farther)));
  }

}
