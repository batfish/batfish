package org.batfish.grammar.cisco_xr;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Prefix.strict;
import static org.batfish.datamodel.RoutingProtocol.BGP;
import static org.batfish.datamodel.bgp.community.ExtendedCommunity.target;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.grammar.cisco_xr.XrGrammarTest.TESTCONFIGS_PREFIX;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of VRF leaking on IOS-XR. */
public final class XrVrfLeakingTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    try {
      return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void testVrfLeaking() {
    String hostname = "xr-vrf-leaking";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    Map<String, Set<AbstractRoute>> routesByVrf = getBgpRoutes(dp.getRibs()).get(hostname);

    //// DEFAULT VRF
    // default vrf should have prefixes leaked from v1 allowed by rp-v1-export-default.
    // rp1-v1-export-default should have added rt:1:0.
    assertRoute(routesByVrf, DEFAULT_VRF_NAME, strict("1.0.1.0/30"), target(1L, 0L));
    assertRoute(routesByVrf, DEFAULT_VRF_NAME, strict("1.0.1.4/30"), target(1L, 0L));
    assertNoRoute(routesByVrf, DEFAULT_VRF_NAME, strict("1.0.1.8/30"));

    // default vrf should have prefixes leaked from v2 allowed by rp-v2-export-default.
    // v2 has explicit export rt(s), which should be added to route.
    assertRoute(
        routesByVrf, DEFAULT_VRF_NAME, strict("1.0.2.0/30"), target(2L, 1L), target(2L, 11L));
    assertRoute(
        routesByVrf, DEFAULT_VRF_NAME, strict("1.0.2.4/30"), target(2L, 1L), target(2L, 11L));
    assertNoRoute(routesByVrf, DEFAULT_VRF_NAME, strict("1.0.2.8/30"));

    //// VRF v1
    // v1 should have prefixes leaked from default vrf
    // Since import policy cannot add rt, and this vrf has no import rt, route should have no
    // communities.
    assertRoute(routesByVrf, "v1", strict("1.0.0.0/30"));
    assertRoute(routesByVrf, "v1", strict("1.0.0.4/30"));
    assertNoRoute(routesByVrf, "v1", strict("1.0.0.8/30"));

    // Since v1 has no import rt, it cannot receive leaked routes from any other non-default VRF,
    // regardless of policy
    assertNoRoute(routesByVrf, "v1", strict("1.0.2.0/30"));
    assertNoRoute(routesByVrf, "v1", strict("1.0.2.4/30"));
    assertNoRoute(routesByVrf, "v1", strict("1.0.2.8/30"));

    //// VRF v2
    // v2 should have prefixes leaked from default vrf.
    // When importing from default VRF, should set v2's import rt.
    assertRoute(routesByVrf, "v2", strict("1.0.0.0/30"), target(1L, 2L), target(2L, 2L));
    assertRoute(routesByVrf, "v2", strict("1.0.0.4/30"), target(1L, 2L), target(2L, 2L));
    assertNoRoute(routesByVrf, "v2", strict("1.0.0.8/30"));

    // v2 should have prefixes from v1 that match any of v2's import route-targets.
    // Note that v1 sets matching import route-targets for two of its prefixes via rp-v1-export
    assertRoute(routesByVrf, "v2", strict("1.0.1.0/30"), target(1L, 2L));
    assertRoute(routesByVrf, "v2", strict("1.0.1.4/30"), target(1L, 2L));
    assertNoRoute(routesByVrf, "v2", strict("1.0.1.8/30"));

    //// VRF v3
    // v3 should have prefixes leaked from v2 that match v2's export policy and v3's import policy
    // The RT should have been set by rp-v2-export.
    // There should be no leaked routes from v1, since v1 never sets an appropriate RT
    assertNoRoute(routesByVrf, "v3", strict("1.0.0.0/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.0.4/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.0.8/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.1.0/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.1.4/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.1.8/30"));
    assertRoute(
        routesByVrf, "v3", strict("1.0.2.0/30"), target(2L, 1L), target(2L, 11L), target(3L, 3L));
    assertNoRoute(routesByVrf, "v3", strict("1.0.2.4/30"));
    assertNoRoute(routesByVrf, "v3", strict("1.0.2.8/30"));
  }

  private void assertRoute(
      Map<String, Set<AbstractRoute>> routesByVrf,
      String vrf,
      Prefix prefix,
      ExtendedCommunity... expectedCommunities) {
    Set<AbstractRoute> routes = routesByVrf.get(vrf);
    assertThat(
        routes,
        hasItem(allOf(hasPrefix(prefix), hasCommunities(CommunitySet.of(expectedCommunities)))));
  }

  private void assertNoRoute(
      Map<String, Set<AbstractRoute>> routesByVrf, String vrf, Prefix prefix) {
    Set<AbstractRoute> routes = routesByVrf.get(vrf);
    assertThat(routes, not(hasItem(hasPrefix(prefix))));
  }

  private Map<String, Map<String, Set<AbstractRoute>>> getBgpRoutes(
      SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs) {
    return toImmutableMap(
        ribs,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey,
                vrfEntry ->
                    vrfEntry.getValue().getRoutes().stream()
                        .filter(r -> r.getProtocol() == BGP)
                        .collect(ImmutableSet.toImmutableSet())));
  }
}
