package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;

/** Tests IPv6 forwarding-information-base behavior. */
public final class Fib6ImplTest {

  @Test
  public void testLongestPrefixAndNextHop() {
    ConnectedRoute6 broad =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8::/32"),
            "Ethernet1");

    Ospfv3IntraAreaRoute6 specific =
        new Ospfv3IntraAreaRoute6(
            Prefix6.parse("2001:db8:10::/64"),
            "Ethernet2",
            Ip6.parse("2001:db8:12::2"),
            110,
            20,
            0L);

    Fib6 fib =
        new Fib6Impl(
            FinalMainRib6.of(
                broad,
                specific));

    Set<FibEntry6> specificEntries =
        fib.get(
            Ip6.parse(
                "2001:db8:10::1234"));

    assertThat(
        specificEntries,
        hasSize(1));

    FibEntry6 specificEntry =
        specificEntries.iterator().next();

    assertThat(
        specificEntry.getTopLevelRoute(),
        equalTo(specific));

    assertThat(
        specificEntry.getInterfaceName(),
        equalTo("Ethernet2"));

    assertThat(
        specificEntry.getNextHopIp().orElseThrow(),
        equalTo(
            Ip6.parse(
                "2001:db8:12::2")));

    Set<FibEntry6> broadEntries =
        fib.get(
            Ip6.parse(
                "2001:db8:ffff::1"));

    assertThat(
        broadEntries,
        hasSize(1));

    FibEntry6 broadEntry =
        broadEntries.iterator().next();

    assertThat(
        broadEntry.getTopLevelRoute(),
        equalTo(broad));

    assertThat(
        broadEntry.getInterfaceName(),
        equalTo("Ethernet1"));

    assertThat(
        broadEntry.getNextHopIp().isEmpty(),
        equalTo(true));

    assertThat(
        fib.get(
            Ip6.parse(
                "2001:db9::1")),
        equalTo(
            ImmutableSet.of()));
  }

  @Test
  public void testEcmpPreserved() {
    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:20::/64");

    Ospfv3IntraAreaRoute6 route1 =
        new Ospfv3IntraAreaRoute6(
            prefix,
            "Ethernet1",
            Ip6.parse(
                "2001:db8:1::1"),
            110,
            20,
            0L);

    Ospfv3IntraAreaRoute6 route2 =
        new Ospfv3IntraAreaRoute6(
            prefix,
            "Ethernet2",
            Ip6.parse(
                "2001:db8:2::1"),
            110,
            20,
            0L);

    Fib6 fib =
        new Fib6Impl(
            FinalMainRib6.of(
                route1,
                route2));

    assertThat(
        fib.get(
            Ip6.parse(
                "2001:db8:20::abcd")),
        hasSize(2));

    assertThat(
        fib.allEntries(),
        hasSize(2));
  }
}
