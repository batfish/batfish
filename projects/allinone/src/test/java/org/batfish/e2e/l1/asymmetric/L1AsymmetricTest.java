package org.batfish.e2e.l1.asymmetric;

import static org.hamcrest.Matchers.containsInAnyOrder;
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

/** Tests that a network with IP Reuse and asymmetric L1 topology is not connected redundantly. */
public class L1AsymmetricTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Topology getLayer3Edges() throws IOException {
    String prefix = "org/batfish/e2e/l1/asymmetric";
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(prefix, "a1", "a2", "b1", "b2")
                .setLayer1TopologyPrefix(prefix + "/batfish")
                .build(),
            _folder);
    return batfish.getTopologyProvider().getInitialLayer3Topology(batfish.getSnapshot());
  }

  @Test
  public void testLayer3Connectivity() throws IOException {
    Edge forward1 =
        new Edge(NodeInterfacePair.of("a1", "Ethernet1"), NodeInterfacePair.of("b1", "Ethernet1"));
    Edge forward2 =
        new Edge(NodeInterfacePair.of("a2", "Ethernet1"), NodeInterfacePair.of("b2", "Ethernet1"));
    // NB: should not contain a1-b2 or b2-a1.
    assertThat(
        getLayer3Edges().getEdges(),
        containsInAnyOrder(forward1, forward1.reverse(), forward2, forward2.reverse()));
  }
}
