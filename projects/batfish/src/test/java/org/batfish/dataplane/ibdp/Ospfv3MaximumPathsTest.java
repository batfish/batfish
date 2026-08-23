package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

/** Tests for OSPFv3 maximum-paths routing-table behavior. */
public final class Ospfv3MaximumPathsTest {

  private static AbstractRoute6 route(
      String iface,
      String nextHop) {

    return new Ospfv3IntraAreaRoute6(
        Prefix6.parse(
            "2001:db8:100::/64"),
        iface,
        Ip6.parse(nextHop),
        110,
        20,
        0L);
  }

  private static Set<AbstractRoute6>
      sixEqualCostRoutes() {

    return ImmutableSet.of(
        route(
            "Ethernet1",
            "2001:db8:1::1"),
        route(
            "Ethernet2",
            "2001:db8:2::1"),
        route(
            "Ethernet3",
            "2001:db8:3::1"),
        route(
            "Ethernet4",
            "2001:db8:4::1"),
        route(
            "Ethernet5",
            "2001:db8:5::1"),
        route(
            "Ethernet6",
            "2001:db8:6::1"));
  }

  @Test
  public void testDefaultFourPaths() {
    Set<AbstractRoute6> allRoutes =
        sixEqualCostRoutes();

    Set<AbstractRoute6> installed =
        Ospfv3RoutingProcess
            .selectRoutingRoutes(
                allRoutes,
                null,
                4);

    /*
     * maximum-paths affects routing-table installation only.
     * The caller's OSPF control-plane route set remains complete.
     */
    assertThat(
        allRoutes.size(),
        equalTo(6));

    assertThat(
        installed.size(),
        equalTo(4));

    assertThat(
        installed.stream()
            .map(
                AbstractRoute6::
                    getNextHopInterface)
            .collect(
                java.util.stream.Collectors
                    .toSet()),
        equalTo(
            Set.of(
                "Ethernet1",
                "Ethernet2",
                "Ethernet3",
                "Ethernet4")));
  }

  @Test
  public void testConfiguredTwoPaths() {
    Set<AbstractRoute6> installed =
        Ospfv3RoutingProcess
            .selectRoutingRoutes(
                sixEqualCostRoutes(),
                null,
                2);

    assertThat(
        installed.size(),
        equalTo(2));

    assertThat(
        installed.stream()
            .map(
                AbstractRoute6::
                    getNextHopInterface)
            .collect(
                java.util.stream.Collectors
                    .toSet()),
        equalTo(
            Set.of(
                "Ethernet1",
                "Ethernet2")));
  }

  @Test
  public void testLimitIsPerPrefix() {
    Set<AbstractRoute6> routes =
        ImmutableSet.of(
            route(
                "Ethernet1",
                "2001:db8:1::1"),
            route(
                "Ethernet2",
                "2001:db8:2::1"),
            new Ospfv3IntraAreaRoute6(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                "Ethernet3",
                Ip6.parse(
                    "2001:db8:3::1"),
                110,
                20,
                0L),
            new Ospfv3IntraAreaRoute6(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                "Ethernet4",
                Ip6.parse(
                    "2001:db8:4::1"),
                110,
                20,
                0L));

    Set<AbstractRoute6> installed =
        Ospfv3RoutingProcess
            .selectRoutingRoutes(
                routes,
                null,
                1);

    /*
     * One path is retained for each destination, not one path
     * for the whole process.
     */
    assertThat(
        installed.size(),
        equalTo(2));

    assertThat(
        installed.stream()
            .map(AbstractRoute6::getNetwork)
            .collect(
                java.util.stream.Collectors
                    .toSet()),
        equalTo(
            Set.of(
                Prefix6.parse(
                    "2001:db8:100::/64"),
                Prefix6.parse(
                    "2001:db8:200::/64"))));
  }
}
