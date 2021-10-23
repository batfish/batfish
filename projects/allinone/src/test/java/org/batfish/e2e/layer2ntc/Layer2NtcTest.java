package org.batfish.e2e.layer2ntc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A test of an issue reported by {@code Sharon Saadon} on Slack. Link:
 * https://networktocode.slack.com/archives/CCE02JK7T/p1619679666156900
 *
 * <p>The network is a simple star topology with a central switch sw-1 at the center that has two
 * routers (one inward facing rtr-1 and one outward facing rtr-isp-1) and a firewall attached. The a
 * self-loop on fw-1 is used to bridge vlan10 and vlan20 and connect the inner and outer routers
 * through the firewall.
 */
public class Layer2NtcTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Topology getLayer3Edges(String nameOfBatfishFolderContainingL1) throws IOException {
    String prefix = "org/batfish/e2e/layer2ntc";
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(prefix, "fw-1.cfg", "rtr-1.cfg", "rtr-isp-1.cfg", "sw-1.cfg")
                .setLayer1TopologyPrefix(prefix + '/' + nameOfBatfishFolderContainingL1)
                .build(),
            _folder);
    batfish.loadConfigurations(batfish.getSnapshot());
    return batfish.getTopologyProvider().getInitialLayer3Topology(batfish.getSnapshot());
  }

  /** When the entire l1 topology is present, the L3 edge comes up. */
  @Test
  public void testLayer3Connectivity() throws IOException {
    Edge forward =
        new Edge(
            NodeInterfacePair.of("rtr-1", "xe-0/0/0.10"),
            NodeInterfacePair.of("rtr-isp-1", "xe-0/0/1.20"));
    assertThat(
        getLayer3Edges("batfish").getEdges(), containsInAnyOrder(forward, forward.reverse()));
  }

  /**
   * When the self-loop is missing from l1 topology (the interfaces are connected through a missing
   * device instead), the L3 edge does not come up.
   */
  @Test
  public void testLayer3ConnectivityMissingLoop() throws IOException {
    assertThat(getLayer3Edges("batfish-missing-loop").getEdges(), empty());
  }
}
