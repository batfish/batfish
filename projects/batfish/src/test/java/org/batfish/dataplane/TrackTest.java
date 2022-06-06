package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end test of dataplane-dependent static route tracking on NX-OS devices. */
public class TrackTest {
  private static final String SNAPSHOT_FOLDER = "org/batfish/dataplane/testrigs/track";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  /*
   * Topology:
   * r1 <============================> r2
   *    Ethernet1/1 GigabitEthernet0/0
   *    .1      192.0.2.0/24        .2
   * - r1 is running HMM on Vlan2, for which Ethernet1/1 is an access switchport
   * - r2 is connected to r1 on GigabitEthernet0/0
   * - r1 main RIB should have HMM route for 192.0.2.2/32 with next-hop Vlan2
   * - r1 should have static routes:
   *   - 10.0.1.0/24, which tracks hmm route 192.0.2.2/32
   *   - 10.0.2.0/24, which tracks route 10.2.0.0/24 from interface Ethernet1/2
   *   - 10.0.3.0/24, which tracks interface Ethernet1/2 line status
   * - r1 should not have static routes:
   *   - 10.0.4.0/24, which tracks missing hmm route 192.0.2.3/32
   *   - 10.0.5.0/24, which tracks missing route 10.3.0.1/24 from shutdown interface Ethernet1/3
   *   - 10.0.6.0/24, which tracks shutdown interface Ethernet1/3
   */
  @Before
  public void setup() throws IOException {
    String r1Filename = "r1";
    String r2Filename = "r2";
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_FOLDER, ImmutableSet.of(r1Filename, r2Filename))
                .setLayer1TopologyPrefix(SNAPSHOT_FOLDER)
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testRoutes() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());

    // test main RIB routes
    Set<AbstractRoute> r1Routes = dp.getRibs().get("r1", DEFAULT_VRF_NAME).getRoutes();

    assertThat(
        r1Routes.stream()
            .filter(r -> r.getProtocol() == RoutingProtocol.STATIC)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            hasPrefix(Prefix.strict("10.0.1.0/24")),
            hasPrefix(Prefix.strict("10.0.2.0/24")),
            hasPrefix(Prefix.strict("10.0.3.0/24"))));
  }

  @Test
  public void testTopology() {
    Topology topology = _batfish.getTopologyProvider().getLayer3Topology(_batfish.getSnapshot());

    assertThat(topology.getEdges(), hasItem(Edge.of("r1", "Vlan2", "r2", "GigabitEthernet0/0")));
  }
}
