package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BgpTopologyChangeTest {
  private static final String SNAPSHOT_PATH = "org/batfish/dataplane/ibdp/bgp-topology-change";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  // A simple network where A-B-C via /31 links, A-B have a BGP session and A-C have a BGP session
  // using A's loopback. The lab is set up with strategic static routes so that the A-C session can
  // only come up after A advertises its loopback to B. Test that after this happens, C gets A's
  // routes.
  @Test
  public void testBgpRoutesAfterTopologyChange() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationFiles(SNAPSHOT_PATH, "a", "b", "c").build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    Set<Bgpv4Route> bgpRoutesOnC =
        dataplane.getBgpRoutes().get("c", Configuration.DEFAULT_VRF_NAME);

    assertThat(
        bgpRoutesOnC,
        containsInAnyOrder(
            hasPrefix(Prefix.parse("1.1.1.1/32")), // a's loopback
            hasPrefix(Prefix.parse("10.0.0.0/31")), // a's Ethernet1
            hasPrefix(Prefix.parse("20.0.0.0/31")))); // a learned from b
  }
}
