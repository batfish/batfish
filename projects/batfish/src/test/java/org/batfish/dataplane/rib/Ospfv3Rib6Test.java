package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3ExternalType1Route6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
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

  @Test
  public void testRouteTypePreference() {
    Prefix6 prefix =
        Prefix6.parse("2001:db8:40::/64");

    Ospfv3ExternalType2Route6 external =
        new Ospfv3ExternalType2Route6(
            prefix,
            "Ethernet3",
            110,
            1,
            Ip.parse("192.0.2.3"));

    Ospfv3InterAreaRoute6 inter =
        new Ospfv3InterAreaRoute6(
            prefix,
            "Ethernet2",
            Ip6.parse("2001:db8:2::2"),
            110,
            500,
            0L);

    Ospfv3IntraAreaRoute6 intra =
        new Ospfv3IntraAreaRoute6(
            prefix,
            "Ethernet1",
            Ip6.parse("2001:db8:1::1"),
            110,
            1000,
            1L);

    Ospfv3Rib6 rib = new Ospfv3Rib6();

    assertThat(
        rib.mergeRoute(external),
        equalTo(true));

    assertThat(
        rib.mergeRoute(inter),
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                inter)));

    assertThat(
        rib.mergeRoute(intra),
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                intra)));
  }

  @Test
  public void testExternalType1PreferredToType2() {
    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:50::/64");

    /*
     * OSPF route-type preference puts E1 ahead of E2 even when
     * the E2 numeric metric is smaller.
     */
    Ospfv3ExternalType2Route6 e2 =
        new Ospfv3ExternalType2Route6(
            prefix,
            "Ethernet2",
            110,
            1L,
            Ip.parse(
                "192.0.2.2"));

    Ospfv3ExternalType1Route6 e1 =
        new Ospfv3ExternalType1Route6(
            prefix,
            "Ethernet1",
            110,
            100L,
            Ip.parse(
                "192.0.2.1"));

    Ospfv3Rib6 rib =
        new Ospfv3Rib6();

    assertThat(
        rib.mergeRoute(e2),
        equalTo(true));

    assertThat(
        rib.mergeRoute(e1),
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet
                .<AbstractRoute6>of(e1)));

    assertThat(
        rib.getBackupRoutes(),
        equalTo(
            ImmutableSet
                .<AbstractRoute6>of(e2)));
  }

  @Test
  public void testExternalType1LowerTotalMetricPreferred() {
    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:60::/64");

    Ospfv3ExternalType1Route6 farther =
        new Ospfv3ExternalType1Route6(
            prefix,
            "Ethernet2",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            75L,
            25L,
            0L,
            50L,
            Ip.parse(
                "192.0.2.1"),
            0L);

    Ospfv3ExternalType1Route6 closer =
        new Ospfv3ExternalType1Route6(
            prefix,
            "Ethernet1",
            Ip6.parse(
                "2001:db8:13::1"),
            110,
            35L,
            25L,
            0L,
            10L,
            Ip.parse(
                "192.0.2.1"),
            0L);

    Ospfv3Rib6 rib =
        new Ospfv3Rib6();

    assertThat(
        rib.mergeRoute(farther),
        equalTo(true));

    assertThat(
        rib.mergeRoute(closer),
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet
                .<AbstractRoute6>of(
                    closer)));
  }

}
