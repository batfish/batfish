package org.batfish.e2e.isp;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** E2E test of ISP modeling */
public class IspModelingTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String SNAPSHOTS_DIR = "org/batfish/e2e/isp";

  private IBatfish setup(String snapshotName, Iterable<String> configFiles) throws IOException {
    String snapshotDir = SNAPSHOTS_DIR + "/" + snapshotName;
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotDir, configFiles)
                .setIspConfigPrefix(snapshotDir + "/batfish")
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  @Test
  public void testBasic() throws IOException {
    IBatfish batfish = setup("basic", ImmutableList.of("as2border1.cfg", "as2border2.cfg"));
    BgpTopology bgpTopology = batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot());

    // TODO: higher fidelity testing

    // internet to two ISPs (4 unidirectional edges) + two ISPs to borders (4 uni edges)
    assertThat(bgpTopology.getGraph().edges(), hasSize(8));
  }
}
