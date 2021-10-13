package org.batfish.dataplane.rib;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link RouteAdvertisement} */
public class RouteAdvertisementTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    ConnectedRoute cr1 = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0");
    ConnectedRoute cr2 = new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet0");

    new EqualsTester()
        .addEqualityGroup(new RouteAdvertisement<>(cr1), new RouteAdvertisement<>(cr1))
        .addEqualityGroup(new RouteAdvertisement<>(cr2))
        .addEqualityGroup(new RouteAdvertisement<>(cr2, false))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    RouteAdvertisement<StaticRoute> ra =
        RouteAdvertisement.<StaticRoute>builder()
            .setRoute(
                StaticRoute.testBuilder()
                    .setAdministrativeCost(1)
                    .setNetwork(Prefix.parse("1.1.1.0/24"))
                    .setNextHopIp(Ip.parse("2.2.2.2"))
                    .build())
            .setWithdrawn(true)
            .build();

    assertThat(ra.toBuilder().build(), equalTo(ra));
  }

  @Test
  public void testThrowsOnNullRoute() {
    thrown.expect(IllegalArgumentException.class);
    RouteAdvertisement.<StaticRoute>builder().build();
  }

  @Test
  public void testAdding() {
    StaticRoute route =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .build();
    RouteAdvertisement<StaticRoute> adv = RouteAdvertisement.adding(route);
    assertThat(adv.getRoute(), sameInstance(route));
    assertFalse(adv.isWithdrawn());
  }

  @Test
  public void testWithdrawing() {
    StaticRoute route =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .build();
    RouteAdvertisement<StaticRoute> adv = RouteAdvertisement.withdrawing(route);
    assertThat(adv.getRoute(), sameInstance(route));
    assertTrue(adv.isWithdrawn());
  }
}
