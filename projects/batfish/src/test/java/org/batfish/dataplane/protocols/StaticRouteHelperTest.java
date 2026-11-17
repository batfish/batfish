package org.batfish.dataplane.protocols;

import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.batfish.dataplane.protocols.StaticRouteHelper.resolveStaticNextHopIp;
import static org.batfish.dataplane.protocols.StaticRouteHelper.shouldActivateNextHopIpRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.Rib;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link StaticRouteHelper} */
final class StaticRouteHelperTest {

  private static boolean shouldActivate(
      StaticRoute sr, Rib rib, ResolutionRestriction<AnnotatedRoute<AbstractRoute>> restriction) {
    return shouldActivateNextHopIpRoute(
        sr, resolveStaticNextHopIp(sr.getNextHopIp(), sr.getRecursive(), rib, restriction));
  }

  private Rib _rib;

  @BeforeEach
  void setup() {
    // Empty rib before each test
    _rib = new Rib();
  }

  /** Check no static routes are activated if RIB is empty */
  @Test
  void testShouldActivateEmptyRib() {
    Ip nextHop = Ip.parse("1.1.1.1");
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(nextHop)
            .setAdministrativeCost(1)
            .build();
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(false));
  }

  /** Do not activate if no match for nextHop IP exists */
  @Test
  void testShouldActivateNoMatch() {
    _rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth0")));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(false));
  }

  /** Activate if next hop IP matches a route */
  @Test
  void testShouldActivateMatch() {
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/8"))
                .setNextHopInterface("Eth0")
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(true));
  }

  /** Do not activate if the route to the next hop IP has same prefix as route in question. */
  @Test
  void testShouldActivateSelfReferential() {
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("9.9.9.0/24"))
                .setNextHopIp(Ip.parse("1.1.1.2"))
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("9.9.9.9"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(false));
  }

  /**
   * Activate a route matching its own next-hop IP if there is a more specific matching route
   * already in the RIB
   */
  @Test
  void testShouldActivateIfExists() {
    StaticRoute matching =
        StaticRoute.testBuilder().setNetwork(Prefix.strict("1.1.1.1/32")).build();
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();
    _rib.mergeRoute(annotateRoute(matching));

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(true));
  }

  /** Activate if route exists for the same prefix but next hop is different */
  @Test
  void testShouldActivateWithDiffNextHops() {
    // base route
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/8"))
                .setNextHopInterface("Eth0")
                .setAdministrativeCost(1)
                .build()));
    // Static route 1, same network as sr, but different next hop ip
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("9.9.9.0/24"))
                .setNextHopIp(Ip.parse("1.1.1.2"))
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(true));
  }

  /** Allow activation in the RIB even if there would be a FIB resolution loop. */
  @Test
  void testShouldActivateWithLoop() {
    /*
     * Route dependency graph
     * 9.9.9.0/24 (nh: 1.1.1.1) --> 1.1.1.0/24 (nh=2.2.2.2) -> 2.2.2.0/24 (nh=9.9.9.9) -> 9.9.9.0/24
     */
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopIp(Ip.parse("2.2.2.2"))
                .setAdministrativeCost(1)
                .build()));
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("2.2.2.0/24"))
                .setNextHopIp(Ip.parse("9.9.9.9"))
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivate(sr, _rib, alwaysTrue()), equalTo(true));
  }

  /**
   * Do not allow installation of a route that would become longest prefix match for its own next
   * hop IP.
   */
  @Test
  void testShouldActivateIfCovered() {
    _rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.parse("9.9.0.0/16"), "Eth0")));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("9.9.9.9"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertFalse(shouldActivate(sr, _rib, alwaysTrue()));
  }

  /**
   * Activate if route is recursive and next hop IP matches a route that is permitted by restriction
   */
  @Test
  void testShouldActivateRecursiveRestrictionPermits() {
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/8"))
                .setNextHopInterface("Eth0")
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertTrue(shouldActivate(sr, _rib, r -> r.getNetwork().getPrefixLength() == 8));
  }

  /**
   * Do not activate if route is recursive but next hop IP matches no route that is permitted by
   * restriction
   */
  @Test
  void testShouldActivateRecursiveRestrictionDenies() {
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/8"))
                .setNextHopInterface("Eth0")
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertFalse(shouldActivate(sr, _rib, r -> r.getNetwork().getPrefixLength() == 16));
  }

  /**
   * Do not activate if route is non-recursive and the only routes matching next hop IP are
   * non-connected
   */
  @Test
  void testShouldActivateNonRecursiveNoConnected() {
    _rib.mergeRoute(
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/8"))
                .setNextHopInterface("Eth0")
                .setAdministrativeCost(1)
                .build()));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .setRecursive(false)
            .build();

    // Test & Assert
    assertFalse(shouldActivate(sr, _rib, alwaysTrue()));
  }

  /**
   * Activate if route is non-recursive and the next hop IP matches a connected route, even if the
   * connected route does not match the restriction.
   */
  @Test
  void testShouldActivateNonRecursiveConnected() {
    _rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "Eth0")));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .setRecursive(false)
            .build();

    // Test & Assert
    assertTrue(shouldActivate(sr, _rib, r -> false));
  }
}
