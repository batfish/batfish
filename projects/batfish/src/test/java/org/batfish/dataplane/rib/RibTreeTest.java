package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.junit.Test;

public final class RibTreeTest {
  @Test
  public void testRemoveRouteGetDelta() {
    StaticRoute.Builder srb =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setNextHop(NextHopDiscard.instance());
    StaticRoute r1 = srb.setAdministrativeCost(2).setTag(1L).build();
    StaticRoute r2 = srb.setAdministrativeCost(2).setTag(2L).build();
    StaticRoute r3 = srb.setAdministrativeCost(1).setTag(3L).build();

    AbstractRib<StaticRoute> owner =
        new AbstractRib<StaticRoute>() {
          @Override
          public int comparePreference(StaticRoute lhs, StaticRoute rhs) {
            // prefer routes with lower admin cost
            return Long.compare(rhs.getAdministrativeCost(), lhs.getAdministrativeCost());
          }
        };
    RibTree<StaticRoute> ribTree = new RibTree<>(owner);
    RibDelta<StaticRoute> addR1 = ribTree.mergeRoute(r1);
    assertThat(addR1, equalTo(RibDelta.adding(r1)));
    assertThat(ribTree.getNumRoutes(), equalTo(1));
    RibDelta<StaticRoute> addR2 = ribTree.mergeRoute(r2);
    assertThat(addR2, equalTo(RibDelta.adding(r2)));
    assertThat(ribTree.getNumRoutes(), equalTo(2));
    RibDelta<StaticRoute> addR3 = ribTree.mergeRoute(r3);
    assertThat(
        addR3,
        equalTo(
            RibDelta.builder()
                .remove(r1, Reason.REPLACE)
                .remove(r2, Reason.REPLACE)
                .add(r3)
                .build()));
    assertThat(ribTree.getNumRoutes(), equalTo(1));
  }
}
