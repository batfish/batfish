package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

/** Tests finalized IPv6 main RIB behavior. */
public final class FinalMainRib6Test {

  @Test
  public void testRoutesAndLongestPrefixMatch() {
    ConnectedRoute6 broad =
        new ConnectedRoute6(
            Prefix6.parse(
                "2001:db8::/32"),
            "Ethernet1");

    Ospfv3IntraAreaRoute6 specific =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse(
                "2001:db8:1::/64"),
            "Ethernet2",
            Ip6.parse(
                "2001:db8:12::2"),
            110,
            20,
            0L);

    FinalMainRib6 rib =
        FinalMainRib6.of(
            broad, specific);

    assertThat(
        rib.getRoutes(),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                broad, specific)));

    assertThat(
        rib.getRoutes(
            Prefix6.parse(
                "2001:db8:1::/64")),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                specific)));

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse(
                "2001:db8:1::1234")),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                specific)));

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse(
                "2001:db8:ffff::1")),
        equalTo(
            ImmutableSet.<AbstractRoute6>of(
                broad)));

    assertThat(
        rib.longestPrefixMatch(
            Ip6.parse(
                "2001:db9::1")),
        equalTo(
            ImmutableSet.of()));
  }

  @Test
  public void testEquality() {
    ConnectedRoute6 route =
        new ConnectedRoute6(
            Prefix6.parse(
                "2001:db8:5::/64"),
            "Ethernet5");

    assertThat(
        FinalMainRib6.of(route),
        equalTo(
            FinalMainRib6.of(route)));
  }
}
