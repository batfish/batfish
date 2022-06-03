package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end-ish test of EVPN Type 5 routes and VTEP forwarding on NX-OS devices */
public class HmmTest {
  private static final String SNAPSHOT_FOLDER = "org/batfish/dataplane/testrigs/hmm";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  /*
   * Topology:
   * r1 <============================> r2
   *    Ethernet1/1 GigabitEthernet0/0
   *    .1      192.0.2.0/24        .2(concrete),.3(vrrp)
   * - r1 is running HMM on Vlan2, for which Ethernet1/1 is an access switchport
   * - r2 is connected to r1 on GigabitEthernet0/0
   *   - GigabitEthernet0/0 has concrete address 192.0.2.2/24, and vrrp ip 192.0.2.3
   * - r1 is not running VRRP
   * - r1 main RIB should have HMM routes for 192.0.2.2/32 and 192.0.2.3/32, with next-hop Vlan2
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
        r1Routes,
        allOf(
            hasItem(
                allOf(
                    hasPrefix(Prefix.strict("192.0.2.2/32")),
                    hasNextHop(NextHopInterface.of("Vlan2")),
                    hasProtocol(RoutingProtocol.HMM))),
            hasItem(
                allOf(
                    hasPrefix(Prefix.strict("192.0.2.3/32")),
                    hasNextHop(NextHopInterface.of("Vlan2")),
                    hasProtocol(RoutingProtocol.HMM)))));
  }

  @Test
  public void testTopology() {
    Topology topology = _batfish.getTopologyProvider().getLayer3Topology(_batfish.getSnapshot());

    assertThat(topology.getEdges(), hasItem(Edge.of("r1", "Vlan2", "r2", "GigabitEthernet0/0")));
  }
}
