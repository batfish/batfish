package org.batfish.dataplane.rib;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterableOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

/** Tests of {@link Rib} */
public class RibTest {
  @Test
  public void testCreatedEmpty() {
    assertThat(new Rib().getRoutes(), emptyIterableOf(AbstractRoute.class));
  }

  @Test
  public void testNonRoutingIsNotInstalled() {
    Rib rib = new Rib();
    AbstractRoute route =
        StaticRoute.builder()
            .setNextHopInterface("foo")
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    route.setNonRouting(true);

    assertThat(rib.mergeRouteGetDelta(route), is(nullValue()));
    assertThat(rib.mergeRoute(route), equalTo(false));
  }

  @Test
  public void testComparePreferenceAdmin() {
    Rib rib = new Rib();
    // Identical routes, different admin distance.
    StaticRoute.Builder sb =
        StaticRoute.builder().setNextHopInterface("foo").setNetwork(Prefix.ZERO);
    AbstractRoute route1 = sb.setAdministrativeCost(100).build();
    AbstractRoute route2 = sb.setAdministrativeCost(101).build();

    assertThat(rib.comparePreference(route1, route2), greaterThan(0));
    assertThat(rib.comparePreference(route2, route1), lessThan(0));
  }

  @Test
  public void testComparePreferenceAdminEqual() {
    Rib rib = new Rib();
    // Identical routes should be equally preferred
    StaticRoute.Builder sb =
        StaticRoute.builder()
            .setNextHopInterface("foo")
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(100);
    AbstractRoute route1 = sb.build();
    AbstractRoute route2 = sb.build();

    assertThat(rib.comparePreference(route1, route2), equalTo(0));
  }

  @Test
  public void testNonForwardingIsNotRoutable() {
    Rib rib = new Rib();
    StaticRoute.Builder sb =
        StaticRoute.builder()
            .setNextHopInterface("foo")
            .setNetwork(Prefix.ZERO)
            .setNonForwarding(true)
            .setAdministrativeCost(100);
    rib.mergeRoute(sb.build());
    assertThat(rib.getRoutableIps(), not(containsIp(Prefix.ZERO.getStartIp())));
  }

  @Test
  public void testNonForwardingMatchesNothing() {
    Rib rib = new Rib();
    StaticRoute.Builder sb =
        StaticRoute.builder().setNextHopInterface("foo").setAdministrativeCost(100);
    Prefix prefix1 = Prefix.parse("1.0.0.0/8");
    Prefix prefix11 = Prefix.parse("1.1.0.0/16");
    rib.mergeRoute(sb.setNetwork(prefix1).build());

    sb.setNonForwarding(true);
    rib.mergeRoute(sb.setNetwork(prefix11).build());
    rib.mergeRoute(sb.setNetwork(Prefix.ZERO).build());

    // no entries for non-forwarding routes
    Map<Prefix, IpSpace> matchingIps = rib.getMatchingIps();
    assertThat(matchingIps, not(hasKey(Prefix.ZERO)));
    assertThat(matchingIps, not(hasKey(prefix11)));

    // non-forwarding routes don't subtract from the match space of forwarding routes
    assertThat(matchingIps.get(prefix1), containsIp(prefix11.getStartIp()));
  }
}
