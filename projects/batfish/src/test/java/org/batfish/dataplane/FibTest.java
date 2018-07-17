package org.batfish.dataplane;

import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibImpl;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.Rib;
import org.junit.Test;

/** Test of {@link Fib} that depend on dataplane behavior. */
public class FibTest {
  @Test
  public void testNonForwardingRouteNotInFib() {
    Rib rib = new Rib();

    StaticRoute nonForwardingRoute =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("Eth1")
            .build();
    nonForwardingRoute.setNonForwarding(true);
    StaticRoute forwardingRoute =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopInterface("Eth1")
            .build();

    rib.mergeRoute(nonForwardingRoute);
    rib.mergeRoute(forwardingRoute);

    Fib fib = new FibImpl(rib);
    Set<AbstractRoute> fibRoutes = fib.getRoutesByNextHopInterface().get("Eth1");

    assertThat(fibRoutes, not(hasItem(hasPrefix(Prefix.parse("1.1.1.0/24")))));
    assertThat(fibRoutes, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));
  }
}
