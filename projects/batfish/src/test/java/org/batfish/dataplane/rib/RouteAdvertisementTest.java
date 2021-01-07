package org.batfish.dataplane.rib;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
        .addEqualityGroup(new RouteAdvertisement<>(cr2, Reason.REPLACE))
        .addEqualityGroup(new RouteAdvertisement<>(cr2, Reason.WITHDRAW))
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
            .setReason(Reason.WITHDRAW)
            .build();

    assertThat(ra.toBuilder().build(), equalTo(ra));
  }

  @Test
  public void testIsWithdrawn() {
    RouteAdvertisement.Builder<StaticRoute> raBuilder =
        RouteAdvertisement.<StaticRoute>builder()
            .setRoute(
                StaticRoute.testBuilder()
                    .setAdministrativeCost(1)
                    .setNetwork(Prefix.parse("1.1.1.0/24"))
                    .setNextHopIp(Ip.parse("2.2.2.2"))
                    .build());

    // Advertisement with Reason.ADD should not be withdrawn
    assertThat(raBuilder.setReason(Reason.ADD).build().isWithdrawn(), equalTo(false));

    // Advertisement with Reason.REPLACE or Reason.WITHDRAW should be withdrawn
    assertThat(raBuilder.setReason(Reason.REPLACE).build().isWithdrawn(), equalTo(true));
    assertThat(raBuilder.setReason(Reason.WITHDRAW).build().isWithdrawn(), equalTo(true));
  }

  @Test
  public void testThrowsOnNullRoute() {
    thrown.expect(IllegalArgumentException.class);
    RouteAdvertisement.<StaticRoute>builder().setReason(Reason.ADD).build();
  }
}
