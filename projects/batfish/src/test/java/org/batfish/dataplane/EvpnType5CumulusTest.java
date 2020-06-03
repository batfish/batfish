package org.batfish.dataplane;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopInterface;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType5RouteThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EvpnType5CumulusTest {
  private static final String SNAPSHOT_PREFIX = "org/batfish/dataplane/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test(expected = AssertionError.class) // xfail this until type 5 routes aren't broken
  public void testType5RoutePresence() throws IOException {
    String snapshotName = "evpn-type5-routes";
    List<String> configurationNames =
        ImmutableList.of(
            "leaf1", "leaf2", "leaf3", "leaf4", "spine1", "spine2", "exitgateway", "internet");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_PREFIX + snapshotName, configurationNames)
                .setLayer1TopologyPrefix(SNAPSHOT_PREFIX + snapshotName)
                .build(),
            _folder);

    batfish.computeDataPlane(batfish.getSnapshot()); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();
    String vrf1 = "vrf1";
    final ImmutableList<String> leafs = ImmutableList.of("leaf1", "leaf2", "leaf3", "leaf4");

    ImmutableSet<Prefix> prefixes =
        ImmutableSet.of(
            Ip.parse("100.100.100.100").toPrefix(), Ip.parse("100.100.100.101").toPrefix());

    Map<String, Set<String>> nextHopInterfaces =
        ImmutableMap.of(
            "leaf1",
            ImmutableSet.of("swp1", "swp11"),
            "leaf2",
            ImmutableSet.of("swp2", "swp12"),
            "leaf3",
            ImmutableSet.of("swp3", "swp13"),
            "leaf4",
            ImmutableSet.of("swp4", "swp14"));

    for (String leaf : leafs) {
      Set<AbstractRoute> routes = ribs.get(leaf).get(vrf1).getRoutes();
      for (Prefix prefix : prefixes) {
        for (String nextHopIface : nextHopInterfaces.get(leaf)) {
          assertThat(
              routes,
              hasItem(
                  isEvpnType5RouteThat(
                      allOf(hasPrefix(prefix), hasNextHopInterface(nextHopIface)))));
        }
      }
    }
  }
}
