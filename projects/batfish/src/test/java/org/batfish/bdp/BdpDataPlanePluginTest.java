package org.batfish.bdp;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishTestUtils;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BdpDataPlanePlugin}. */
public class BdpDataPlanePluginTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test(timeout = 5000)
  public void testComputeFixedPoint() throws IOException {
    SortedMap<String, Configuration> configurations = new TreeMap<>();
    //creating configurations with no vrfs
    configurations.put("h1", BatfishTestUtils.createConfiguration("h1", "eth0"));
    configurations.put("h2", BatfishTestUtils.createConfiguration("h2", "e0"));
    Batfish batfish = BatfishTestUtils.getBatfishWithConfigurations(configurations, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    //Test that compute Data Plane finishes in a finite time
    dataPlanePlugin.computeDataPlane(false);
  }
}
