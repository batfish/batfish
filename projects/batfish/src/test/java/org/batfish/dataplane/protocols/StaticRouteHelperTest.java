package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.batfish.dataplane.protocols.StaticRouteHelper.shouldActivateNextHopIpRoute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.Rib;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link StaticRouteHelper} */
public class StaticRouteHelperTest {

  private Rib _rib;

  @Before
  public void setup() {
    // Empty rib before each test
    _rib = new Rib();
  }

  /** Check no static routes are activated if RIB is empty */
  @Test
  public void testShouldActivateEmptyRib() {
    Ip nextHop = Ip.parse("1.1.1.1");
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(nextHop)
            .setAdministrativeCost(1)
            .build();
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Do not activate if no match for nextHop IP exists */
  @Test
  public void testShouldActivateNoMatch() {
    _rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth0")));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Activate if next hop IP matches a route */
  @Test
  public void testShouldActivateMatch() {
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
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Do not activate if the route to the next hop IP has same prefix as route in question. */
  @Test
  public void testShouldActivateSelfReferential() {
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
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Activate the route with next hop IP within route's prefix, if it is already in the RIB */
  @Test
  public void testShouldActivateIfExists() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();
    _rib.mergeRoute(annotateRoute(sr));

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Activate if route exists for the same prefix but next hop is different */
  @Test
  public void testShouldActivateWithDiffNextHops() {
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
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Allow activation in the RIB even if there would be a FIB resolution loop. */
  @Test
  public void testShouldActivateWithLoop() {
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
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Allow installation of a covered/more specific route */
  @Test
  public void testShouldActivateIfCovered() {
    _rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.parse("9.9.0.0/16"), "Eth0")));

    // Route in question
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(Ip.parse("9.9.9.9"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }
}
