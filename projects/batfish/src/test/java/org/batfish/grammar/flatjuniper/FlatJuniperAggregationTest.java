package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.Topology;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of Juniper aggregate-ethernet devices. */
public class FlatJuniperAggregationTest {

  private static final String SNAPSHOT_PATH = "org/batfish/grammar/juniper/testrigs/aggregation";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testAe0LinkComesUp() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_PATH, Arrays.asList("ae1", "ae2"))
                .build(),
            _folder);
    SortedMap<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    Topology t = batfish.getTopologyProvider().getInitialLayer3Topology(batfish.getSnapshot());
    // Ensure port channel members and bandwidth is setup correctly for the logical ae1.0/ae1.1
    // interfaces
    assertThat(configs.get("ae1"), hasInterface("ae1.0", hasBandwidth(1e9)));
    assertThat(configs.get("ae1"), hasInterface("ae1.1", hasBandwidth(1e9)));
    assertThat(
        configs.get("ae1").getAllInterfaces().get("ae1").getDependencies(),
        contains(new Dependency("ge-0/0/0", DependencyType.AGGREGATE)));
    assertThat(
        t.getEdges(),
        containsInAnyOrder(
            Edge.of("ae1", "ae1.0", "ae2", "ae2.0"),
            Edge.of("ae2", "ae2.0", "ae1", "ae1.0"),
            Edge.of("ae1", "ae1.1", "ae2", "ae2.1"),
            Edge.of("ae2", "ae2.1", "ae1", "ae1.1")));
  }
}
