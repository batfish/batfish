package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasRouteDistinguisher;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType5RouteThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EvpnType5CumulusTest {
  private static final String SNAPSHOT_PREFIX = "org/batfish/grammar/cumulus_nclu/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testType5RoutePresence() throws IOException {
    String snapshotName = "evpn-type5-routes";
    List<String> configurationNames =
        ImmutableList.of("leaf1", "leaf2", "spine", "exitgw", "internet");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(SNAPSHOT_PREFIX + snapshotName, configurationNames)
                .setLayer1TopologyText(SNAPSHOT_PREFIX + snapshotName)
                .build(),
            _folder);

    batfish.computeDataPlane(); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane();

    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();
    Set<AbstractRoute> leaf1Routes = ribs.get("leaf1").get(DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> leaf2Routes = ribs.get("leaf1").get(DEFAULT_VRF_NAME).getRoutes();

    Prefix prefix = Prefix.parse("4.4.4.4/32");

    Ip originIp = Ip.parse("2.2.2.2");
    RouteDistinguisher routeDistinguisher = RouteDistinguisher.from(originIp, 2);
    assertThat(
        leaf1Routes,
        hasItem(
            isEvpnType5RouteThat(
                allOf(hasPrefix(prefix), hasRouteDistinguisher(routeDistinguisher)))));
    assertThat(
        leaf2Routes,
        hasItem(
            isEvpnType5RouteThat(
                allOf(hasPrefix(prefix), hasRouteDistinguisher(routeDistinguisher)))));
  }
}
