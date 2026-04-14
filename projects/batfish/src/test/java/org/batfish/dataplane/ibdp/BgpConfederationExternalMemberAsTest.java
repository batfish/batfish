package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.graph.ValueGraph;
import java.io.IOException;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
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

  private static boolean isSessionEstablished(
      Batfish batfish, String hostname, Ip remotePeerIp) {
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpGraph =
        batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot()).getGraph();
    BgpPeerConfigId peerId =
        new BgpPeerConfigId(hostname, DEFAULT_VRF_NAME, Prefix.create(remotePeerIp, 32), false);
    return bgpGraph.degree(peerId) > 0;
  }

  @Test
  public void testExternalPeerMatchingConfedMemberAs() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-confederation-external-member-as";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationFiles(snapshotPath, "r1", "r2", "r3").build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    // R1↔R2 session should NOT establish: R1's AS (65002) matches a confederation member-AS,
    // causing an OPEN message AS mismatch.
    assertFalse(isSessionEstablished(batfish, "r1", Ip.parse("10.0.12.2")));
    assertFalse(isSessionEstablished(batfish, "r2", Ip.parse("10.0.12.1")));

    // R2↔R3 session (confed-iBGP within the confederation) should establish normally.
    assertTrue(isSessionEstablished(batfish, "r2", Ip.parse("10.0.23.3")));
    assertTrue(isSessionEstablished(batfish, "r3", Ip.parse("10.0.23.2")));
  }
}
