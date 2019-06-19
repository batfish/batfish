package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType3RouteThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EvpnCumulusTest {
  private static final String SNAPSHOT_FOLDER =
      "org/batfish/grammar/cumulus_nclu/testrigs/evpn-l2-vnis";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRoutePresence() throws IOException {
    final String leaf1 = "Leaf1";
    final String leaf2 = "Leaf2";
    final String spine = "Spine";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(SNAPSHOT_FOLDER, ImmutableSet.of(leaf1, leaf2, spine))
                .setLayer1TopologyText(SNAPSHOT_FOLDER)
                .build(),
            _folder);
    batfish.computeDataPlane();
    DataPlane dp = batfish.loadDataPlane();

    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();

    assertThat(
        ribs.get(leaf1).get(DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("3.3.3.3/32")))));
    assertThat(
        ribs.get(leaf2).get(DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("1.1.1.1/32")))));
  }
}
