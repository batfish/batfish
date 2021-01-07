package org.batfish.dataplane.rib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

public class AnnotatedRibTest {
  @Test
  public void testContainsRoute() {
    Rib rib = new Rib();
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("iface")
            .build();
    AnnotatedRoute<StaticRoute> ar = new AnnotatedRoute<>(sr, "vrf");
    rib.mergeRoute(new AnnotatedRoute<>(sr, "vrf"));
    assertTrue(rib.containsRoute(ar));
    assertFalse(rib.containsRoute(new AnnotatedRoute<>(sr, "othervrf")));
    assertFalse(
        rib.containsRoute(
            new AnnotatedRoute<>(
                StaticRoute.testBuilder()
                    .setAdministrativeCost(1)
                    .setNetwork(Prefix.ZERO)
                    .setNextHopInterface("iface2")
                    .build(),
                "othervrf")));
  }
}
