package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasLocalPreference;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test that local preference set by an export route-map is not propagated across EBGP sessions.
 * Local preference is non-transitive and should always be reset to the default on the receiving
 * side.
 *
 * <p>Regression test for https://github.com/batfish/batfish/issues/9262
 */
public final class BgpLocalPrefEbgpTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /**
   * R1 (AS 400) advertises 100.0.0.0/8 to R2 (AS 512) with an outbound route-map that sets
   * local-preference to 50. R2 should install the route with the default local-preference of 100.
   */
  @Test
  public void testEbgpLocalPreferenceReset() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-local-pref-ebgp";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationFiles(snapshotPath, "r1", "r2").build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    Set<Bgpv4Route> bgpRoutesOnR2 =
        dataplane.getBgpRoutes().get("r2", Configuration.DEFAULT_VRF_NAME);

    assertThat(
        bgpRoutesOnR2,
        contains(
            allOf(
                hasPrefix(Prefix.parse("100.0.0.0/8")),
                hasLocalPreference(DEFAULT_LOCAL_PREFERENCE))));
  }
}
