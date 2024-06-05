package org.batfish.e2e.isp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** E2E test of ISP modeling */
public class IspModelingTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String SNAPSHOTS_DIR = "org/batfish/e2e/isp";

  private IBatfish setup(String snapshotName, Iterable<String> configFiles, boolean setupL1)
      throws IOException {
    String snapshotDir = SNAPSHOTS_DIR + "/" + snapshotName;
    TestrigText.Builder testrigText =
        TestrigText.builder()
            .setConfigurationFiles(snapshotDir, configFiles)
            .setIspConfigPrefix(snapshotDir + "/batfish");
    if (setupL1) {
      testrigText.setLayer1TopologyPrefix(snapshotDir + "/batfish");
    }
    IBatfish batfish = BatfishTestUtils.getBatfishFromTestrigText(testrigText.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  // TODO: higher fidelity testing in tests below versus just BGP edge counting

  @Test
  public void testBasic() throws IOException {
    IBatfish batfish = setup("basic", ImmutableList.of("border1.cfg"), false);
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // internet to ISP and ISP to border (4 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(4));
  }

  @Test
  public void testBasicBgpPeerInfo() throws IOException {
    IBatfish batfish = setup("basic-bgppeerinfo", ImmutableList.of("border1.cfg"), false);
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // internet to ISP and ISP to border (4 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(4));
  }

  @Test
  public void testBasicBgpPeerInfoInferredLocalIp() throws IOException {
    IBatfish batfish = setup("basic-bgppeerinfo-implicit", ImmutableList.of("border1.cfg"), false);
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // internet to ISP and ISP to border (4 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(4));
  }

  @Test
  public void testSviPeering() throws IOException {
    IBatfish batfish = setup("svi-peering", ImmutableList.of("border1.cfg", "border2.cfg"), true);
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // internet to ISP, ISP to border1, and ISP to border2 (6 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(6));

    // confirm that we haven't bridged the borders if we are using the new method
    L3Adjacencies l3Adjacencies =
        batfish.getTopologyProvider().getL3Adjacencies(batfish.getSnapshot());
    NodeInterfacePair border1 = NodeInterfacePair.of("border1", "Vlan95");
    NodeInterfacePair border2 = NodeInterfacePair.of("border2", "Vlan95");
    assertFalse(l3Adjacencies.inSameBroadcastDomain(border1, border2));
  }

  @Test
  public void testIspPeering() throws IOException {
    IBatfish batfish = setup("isp-peering", ImmutableList.of("border1.cfg", "border2.cfg"), true);
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // border1 <-> ISP1, border2 <-> ISP2, ISP1 <-> ISP2 (6 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(6));

    // check that routes are propagating between border routers via the two ISPs
    assertThat(
        batfish.loadDataPlane(batfish.getSnapshot()).getBgpRoutes().row("border1").get("default"),
        contains(hasPrefix(Prefix.parse("1.1.1.0/24")), hasPrefix(Prefix.parse("2.2.2.0/24"))));
    assertThat(
        batfish.loadDataPlane(batfish.getSnapshot()).getBgpRoutes().row("border2").get("default"),
        contains(hasPrefix(Prefix.parse("1.1.1.0/24")), hasPrefix(Prefix.parse("2.2.2.0/24"))));
  }
}
