package org.batfish.e2e.layer2ntc;

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

/**
 * A test of an issue reported by {@code Sharon Saadon} on Slack. Link:
 * https://networktocode.slack.com/archives/CCE02JK7T/p1619679666156900
 */
public class Layer2NtcTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testLayer3Connectivity() throws IOException {
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    "org/batfish/e2e/layer2ntc",
                    "fw-1.cfg",
                    "rtr-1.cfg",
                    "rtr-isp-1.cfg",
                    "sw-1.cfg")
                .build(),
            _folder);
    Topology layer3 = batfish.getTopologyProvider().getInitialLayer3Topology(batfish.getSnapshot());

    Edge forward =
        new Edge(
            NodeInterfacePair.of("rtr-1", "xe-0/0/0.10"),
            NodeInterfacePair.of("rtr-isp-1", "xe-0/0/1.20"));
    assertThat(layer3.getEdges(), containsInAnyOrder(forward, forward.reverse()));
  }
}
