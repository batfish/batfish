package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import java.util.Set;
import org.batfish.common.BdpOscillationException;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of route resolution in BGP */
public class BgpResolutionTest {
  private static final String SNAPSHOTS_PREFIX = "org/batfish/dataplane/testrigs/";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testInitiallyUnresolvableExternalRoute() throws IOException {
    /*
     Setup: Devices r1 and r2 share an EBGP peering.
     r1 receives an external advertisement for 4.0.0.0/8, with NHIP 5.5.5.5.
     r1 cannot initially resolve 5.5.5.5, but r2 has a static route for 5.5.5.5 that
     it redistributes into BGP.
     When r1 receives r2's route for 5.5.5.5, it should activate 4.0.0.0/8.
    */
    String snapshotPath = SNAPSHOTS_PREFIX + "bgp-resolution";
    {
      // Confirm that without r2, r1 is unable to resolve the external route
      Batfish batfish =
          BatfishTestUtils.getBatfishFromTestrigText(
              TestrigText.builder()
                  .setExternalBgpAnnouncements(snapshotPath)
                  .setConfigurationFiles(snapshotPath, "r1")
                  .build(),
              _folder);
      batfish.computeDataPlane(batfish.getSnapshot());
      DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
      Set<Bgpv4Route> r1BgpRoutes = dp.getBgpRoutes().get("r1", DEFAULT_VRF_NAME);
      assertThat(r1BgpRoutes, empty());
    }
    {
      // Now include r2; r1 should resolve the external route
      Batfish batfish =
          BatfishTestUtils.getBatfishFromTestrigText(
              TestrigText.builder()
                  .setExternalBgpAnnouncements(snapshotPath)
                  .setConfigurationFiles(snapshotPath, "r1", "r2")
                  .build(),
              _folder);
      batfish.computeDataPlane(batfish.getSnapshot());
      DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
      Set<Bgpv4Route> r1BgpRoutes = dp.getBgpRoutes().get("r1", DEFAULT_VRF_NAME);
      assertThat(
          r1BgpRoutes,
          containsInAnyOrder(
              hasPrefix(Prefix.parse("5.5.5.5/32")), hasPrefix(Prefix.parse("4.0.0.0/8"))));
    }
  }

  @Test
  public void testResolutionLoop() throws IOException {
    /*
     Setup:          r1 -- r2 -- r3

     r1 and r2 have an IBGP peering; r2 and r3 have an EBGP peering.
     r1 and r3 both originate default routes to r2.
     r2 accepts all routes from r1 and sets their local preference to 300 and NHIP to 5.5.5.5.
     r2 accepts all routes from r3 without modifications.
     r2 does not export any routes.
     Should see a loop in dataplane because r2 can't pick which default route to put in main RIB:
     - IBGP default route is preferable (because of local pref), but initially unresolvable
     - EBGP default route is therefore installed first
     - Once EBGP route is installed, IBGP route becomes resolvable and replaces it
     - Main RIB will not install IBGP route because it can only be resolved by itself
     - BGP RIB will deactivate IBGP route upon receiving update from main RIB
     - Cycle repeats
    */
    String snapshotPath = SNAPSHOTS_PREFIX + "bgp-resolution-loop";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationFiles(snapshotPath, "r1", "r2", "r3").build(),
            _folder);
    _thrown.expect(BdpOscillationException.class);
    batfish.computeDataPlane(batfish.getSnapshot());
  }
}
