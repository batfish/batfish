package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.protocols.StaticRouteHelper.isInterfaceRoute;
import static org.batfish.dataplane.protocols.StaticRouteHelper.shouldActivateNextHopIpRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.Rib;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link StaticRouteHelper} */
public class StaticRouteHelperTest {

  private Rib _rib = new Rib();

  @Before
  public void setup() {
    // Empty rib before each test
    _rib = new Rib();
  }

  /** Ensure we identify interface routes correctly. */
  @Test
  public void testIsInterfaceRoute() {

    StaticRoute.Builder sb =
        StaticRoute.builder().setNetwork(Prefix.parse("9.9.9.0/24")).setAdministrativeCost(1);
    Ip someIp = new Ip("1.1.1.1");

    // Unset interface
    StaticRoute sr = sb.setNextHopInterface(Route.UNSET_NEXT_HOP_INTERFACE).build();
    assertThat(isInterfaceRoute(sr), equalTo(false));

    // Unset interface + nextHopIp
    sr = sb.setNextHopInterface(Route.UNSET_NEXT_HOP_INTERFACE).setNextHopIp(someIp).build();
    assertThat(isInterfaceRoute(sr), equalTo(false));

    // Null interface
    sr =
        sb.setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP)
            .build();
    assertThat(isInterfaceRoute(sr), equalTo(true));

    // Null interface + nextHopIp
    sr = sb.setNextHopInterface(Interface.NULL_INTERFACE_NAME).setNextHopIp(someIp).build();
    assertThat(isInterfaceRoute(sr), equalTo(true));

    // Real interface
    sr = sb.setNextHopInterface("Eth0").setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP).build();
    assertThat(isInterfaceRoute(sr), equalTo(true));

    // Real interface + nextHopIp
    sr = sb.setNextHopInterface("Eth0").setNextHopIp(someIp).build();
    assertThat(isInterfaceRoute(sr), equalTo(true));
  }

  /** Check no static routes are activated if RIB is empty */
  @Test
  public void testShouldActivateEmptyRib() {
    Ip nextHop = new Ip("1.1.1.1");
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(nextHop)
            .setAdministrativeCost(1)
            .build();
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Do not activate if no match for nextHop IP exists */
  @Test
  public void testShouldActivateNoMatch() {
    _rib.mergeRoute(new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth0"));

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Activate if next hop IP matches a route */
  @Test
  public void testShouldActivateMatch() {
    _rib.mergeRoute(
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setNextHopInterface("Eth0")
            .setAdministrativeCost(1)
            .build());

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Do not activate if the route to the next hop IP has same prefix as route in question. */
  @Test
  public void testShouldActivateSelfReferential() {
    _rib.mergeRoute(
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("1.1.1.2"))
            .setAdministrativeCost(1)
            .build());

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("9.9.9.9"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(false));
  }

  /** Activate the route with next hop IP within route's prefix, if it is already in the RIB */
  @Test
  public void testShouldActivateIfExists() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(new Ip("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();
    _rib.mergeRoute(sr);

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Activate if route exists for the same prefix but next hop is different */
  @Test
  public void testShouldActivateWithDiffNextHops() {
    // base route
    _rib.mergeRoute(
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setNextHopInterface("Eth0")
            .setAdministrativeCost(1)
            .build());
    // Static route 1, same network as sr, but different next hop ip
    _rib.mergeRoute(
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("1.1.1.2"))
            .setAdministrativeCost(1)
            .build());

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("1.1.1.1"))
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
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(new Ip("2.2.2.2"))
            .setAdministrativeCost(1)
            .build());
    _rib.mergeRoute(
        StaticRoute.builder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(new Ip("9.9.9.9"))
            .setAdministrativeCost(1)
            .build());

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("1.1.1.1"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }

  /** Allow installation of a covered/more specific route */
  @Test
  public void testShouldActivateIfCovered() {
    _rib.mergeRoute(new ConnectedRoute(Prefix.parse("9.9.0.0/16"), "Eth0"));

    // Route in question
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("9.9.9.0/24"))
            .setNextHopIp(new Ip("9.9.9.9"))
            .setAdministrativeCost(1)
            .build();

    // Test & Assert
    assertThat(shouldActivateNextHopIpRoute(sr, _rib), equalTo(true));
  }
}
