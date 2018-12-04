package org.batfish.grammar.flatjuniper;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of Juniper aggregate-ethernet devices. */
public class FlatJuniperAggregationTest {

  private static final String SNAPSHOT_PATH = "org/batfish/grammar/juniper/aggregation";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testAe0LinkComesUp() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(SNAPSHOT_PATH, Arrays.asList("ae1", "ae2"))
                .build(),
            _folder);
    batfish.loadConfigurations();
    Topology t = batfish.getEnvironmentTopology();
    assertThat(
        t.getEdges(),
        containsInAnyOrder(
            Edge.of("ae1", "ae1.0", "ae2", "ae2.0"),
            Edge.of("ae2", "ae2.0", "ae1", "ae1.0"),
            Edge.of("ae1", "ae1.1", "ae2", "ae2.1"),
            Edge.of("ae2", "ae2.1", "ae1", "ae1.1")));
  }
}
