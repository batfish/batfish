package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType3RouteThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end-ish test of EVPN Type 3 routes on cumulus devices */
public class EvpnCumulusTest {
  private static final String SNAPSHOT_FOLDER = "org/batfish/dataplane/testrigs/evpn-l2-vnis";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRoutePresence() throws IOException {
    final String leaf1 = "leaf1";
    final String leaf2 = "leaf2";
    final String spine = "spine";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_FOLDER, ImmutableSet.of(leaf1, leaf2, spine))
                .setLayer1TopologyPrefix(SNAPSHOT_FOLDER)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    Table<String, String, Set<EvpnRoute<?, ?>>> ribs = dp.getEvpnRoutes();

    assertThat(
        ribs.get(leaf1, DEFAULT_VRF_NAME),
        hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("3.3.3.3/32")))));
    assertThat(
        ribs.get(leaf2, DEFAULT_VRF_NAME),
        hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("1.1.1.1/32")))));
  }
}
