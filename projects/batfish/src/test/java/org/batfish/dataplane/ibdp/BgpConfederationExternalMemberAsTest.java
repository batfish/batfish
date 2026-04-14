package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;

import java.io.IOException;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Regression test for https://github.com/batfish/batfish/issues/9877.
 *
 * <p>Topology: R1 (AS 65002, external) --- R2 (sub-AS 65001, confed 65000, peers {65002}) --- R3
 * (sub-AS 65002, confed 65000, peers {65001})
 *
 * <p>R1's AS (65002) matches confederation member R3's sub-AS. R2 treats R1 as a confed-eBGP peer
 * and would send its member-AS (65001) in the OPEN message. R1 expects 65000 (confed ID), causing
 * an AS mismatch. No session should form between R1 and R2.
 */
public final class BgpConfederationExternalMemberAsTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExternalPeerMatchingConfedMemberAs() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-confederation-external-member-as";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationFiles(snapshotPath, "r1", "r2", "r3").build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    Prefix advertisedPrefix = Prefix.parse("1.1.1.1/32");

    // R1 originates the route
    assertThat(
        dataplane.getBgpRoutes().get("r1", DEFAULT_VRF_NAME), hasItem(hasPrefix(advertisedPrefix)));

    // R2 should NOT have R1's route — the R1↔R2 session should not establish because R1's AS
    // (65002) matches a confederation member-AS, causing an OPEN message AS mismatch.
    assertThat(dataplane.getBgpRoutes().get("r2", DEFAULT_VRF_NAME), empty());
  }
}
