package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RipInternalRouteTest {
  @Rule public ExpectedException _expectedException = ExpectedException.none();
  private static final int adminCost =
      RoutingProtocol.RIP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS);

  @Test
  public void testConstructorAcceptsValidMetric() {
    // Test all valid metrics, there are only 16 of them. Just testing that no exceptions are thrown
    for (int m = 0; m <= RipRoute.MAX_ROUTE_METRIC; m++) {
      new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, m, 1L);
    }
  }

  @Test
  public void testConstructorRejectsLowMetric() {
    _expectedException.expect(IllegalArgumentException.class);
    new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, 17, 1L);
  }

  @Test
  public void testConstructorRejectsHighMetric() {
    _expectedException.expect(IllegalArgumentException.class);
    new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, -1, 1L);
  }

  @Test
  public void testEquals() {
    RipInternalRoute r1 =
        new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, 1, 1L);
    RipInternalRoute r1copy =
        new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, 1, 1L);
    // Different metric from r1
    RipInternalRoute r2 =
        new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost, 2, 1L);
    // Different admin cost from r1
    RipInternalRoute r3 =
        new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), adminCost + 1, 1, 1L);
    // Different ip from r1
    RipInternalRoute r4 =
        new RipInternalRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.3"), adminCost, 1, 1L);
    // Different network from r1
    RipInternalRoute r5 =
        new RipInternalRoute(Prefix.parse("1.1.1.2/32"), Ip.parse("2.2.2.2"), adminCost, 2, 1L);

    assertThat(r1, equalTo(r1));
    assertThat(r1, equalTo(r1copy));
    assertThat(r1, not(equalTo(r2)));
    assertThat(r1, not(equalTo(r3)));
    assertThat(r1, not(equalTo(r4)));
    assertThat(r1, not(equalTo(r5)));
    assertThat(r1, not(equalTo(null)));
    assertThat(r1, not(equalTo(Prefix.parse("1.1.1.1/32"))));
  }

  @Test
  public void testToBuilder() {
    RipInternalRoute r =
        RipInternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setNextHop(NextHopDiscard.instance())
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
  }
}
