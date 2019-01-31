package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
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
        .addEqualityGroup(new RouteAdvertisement<>(cr2, true, Reason.REPLACE))
        .addEqualityGroup(new RouteAdvertisement<>(cr2, true, Reason.WITHDRAW))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    RouteAdvertisement<StaticRoute> ra =
        RouteAdvertisement.<StaticRoute>builder()
            .setRoute(
                StaticRoute.builder()
                    .setAdministrativeCost(1)
                    .setNetwork(Prefix.parse("1.1.1.0/24"))
                    .setNextHopIp(Ip.parse("2.2.2.2"))
                    .build())
            .setReason(Reason.WITHDRAW)
            .setWithdraw(true)
            .build();

    assertThat(ra.toBuilder().build(), equalTo(ra));
  }

  @Test
  public void testThrowsOnInvalidWithdrawReason() {
    thrown.expect(IllegalArgumentException.class);
    RouteAdvertisement.<StaticRoute>builder()
        .setRoute(
            StaticRoute.builder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopIp(Ip.parse("2.2.2.2"))
                .build())
        .setReason(Reason.ADD)
        .setWithdraw(true)
        .build();
  }

  @Test
  public void testThrowsOnInvalidAddReason() {
    thrown.expect(IllegalArgumentException.class);
    RouteAdvertisement.<StaticRoute>builder()
        .setRoute(
            StaticRoute.builder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopIp(Ip.parse("2.2.2.2"))
                .build())
        .setReason(Reason.REPLACE)
        .setWithdraw(false)
        .build();
  }

  @Test
  public void testThrowsOnInvalidAddReason2() {
    thrown.expect(IllegalArgumentException.class);
    RouteAdvertisement.<StaticRoute>builder()
        .setRoute(
            StaticRoute.builder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopIp(Ip.parse("2.2.2.2"))
                .build())
        .setReason(Reason.WITHDRAW)
        .setWithdraw(false)
        .build();
  }
}
