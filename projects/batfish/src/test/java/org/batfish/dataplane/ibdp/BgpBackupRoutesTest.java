package org.batfish.dataplane.ibdp;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of backup routes in e/i/overall BGP RIBs. */
public final class BgpBackupRoutesTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /*
   Topology: R1 <=> R2 <=> R3 <=> R4

   - All links have eBGP sessions.
   - R1 and R4 originate a route to 5.5.5.5/32
   - R2 first receives the route from R1
   - Later, R2 receives the route from R4.
   - The route should be in the backup routes of both the eBGP and BGP RIBs
  */
  @Test
  public void testEbgpNeverActiveBackupsAreBgpBackups() throws IOException {
    String snapshotPath =
        "org/batfish/dataplane/ibdp/bgp-backup-routes/ebgp-never-active-backups-are-bgp-backups";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "r1", "r2", "r3", "r4")
                .build(),
            _folder);
    Prefix advPrefix = Prefix.strict("5.5.5.5/32");
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    // r1
    assertThat(
        getOnlyElement(dataplane.getBgpRoutes().get("r1", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.empty()))));
    assertThat(dataplane.getBgpBackupRoutes().get("r1", DEFAULT_VRF_NAME), empty());

    // r2
    assertThat(
        getOnlyElement(dataplane.getBgpRoutes().get("r2", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.ofSingletonAsSets(1L)))));
    assertThat(
        getOnlyElement(dataplane.getBgpBackupRoutes().get("r2", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.ofSingletonAsSets(3L, 4L)))));

    // r3
    assertThat(
        getOnlyElement(dataplane.getBgpRoutes().get("r3", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.ofSingletonAsSets(4L)))));
    assertThat(
        getOnlyElement(dataplane.getBgpBackupRoutes().get("r3", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.ofSingletonAsSets(2L, 1L)))));

    // r4
    assertThat(
        getOnlyElement(dataplane.getBgpRoutes().get("r4", DEFAULT_VRF_NAME)),
        allOf(hasPrefix(advPrefix), hasAsPath(equalTo(AsPath.empty()))));
    assertThat(dataplane.getBgpBackupRoutes().get("r4", DEFAULT_VRF_NAME), empty());
  }
}
