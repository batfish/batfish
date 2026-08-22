package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

/** Tests for the IPv6 RIB foundation. */
public final class Rib6Test {

  @Test
  public void testPreferenceAndBackupPromotion() {
    Rib6 rib = new Rib6();

    ConnectedRoute6 worse =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:1::/64"),
            "Ethernet2",
            20);

    ConnectedRoute6 better =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:1::/64"),
            "Ethernet1",
            10);

    assertThat(rib.mergeRoute(worse), equalTo(true));
    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(worse)));

    assertThat(rib.mergeRoute(better), equalTo(true));
    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(better)));
    assertThat(
        rib.getBackupRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(worse)));

    assertThat(rib.removeRoute(better), equalTo(true));
    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(worse)));
    assertThat(
        rib.getBackupRoutes(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testEqualCostMultipath() {
    Rib6 rib = new Rib6();

    ConnectedRoute6 r1 =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:2::/64"),
            "Ethernet1",
            10);

    ConnectedRoute6 r2 =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:2::/64"),
            "Ethernet2",
            10);

    assertThat(rib.mergeRoute(r1), equalTo(true));
    assertThat(rib.mergeRoute(r2), equalTo(true));

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(r1, r2)));

    assertThat(
        rib.getBackupRoutes(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testLongestPrefixMatch() {
    Rib6 rib = new Rib6();

    ConnectedRoute6 lessSpecific =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8::/32"),
            "Ethernet1");

    ConnectedRoute6 moreSpecific =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:10::/64"),
            "Ethernet2");

    rib.mergeRoute(lessSpecific);
    rib.mergeRoute(moreSpecific);

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse("2001:db8:10::1234")),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                moreSpecific)));

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse("2001:db8:20::1234")),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                lessSpecific)));

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse("2001:db9::1")),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testConnectedRib6() {
    ConnectedRib6 rib = new ConnectedRib6();

    ConnectedRoute6 r1 =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:30::/64"),
            "Ethernet1");

    ConnectedRoute6 r2 =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:30::/64"),
            "Ethernet2");

    rib.mergeRoute(r1);
    rib.mergeRoute(r2);

    assertThat(
        rib.getRoutes(),
        equalTo(ImmutableSet.of(r1, r2)));
  }
}
