package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests that a route reflector with multiple routes for a prefix will advertise only one route per
 * unique next hop for that given prefix.
 */
public final class BgpAddPathUniqueNextHopTest {

  private static final Prefix PREFIX = Prefix.strict("10.0.0.0/32");
  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private DataPlane _dp;

  /*
   Topology: {as1r1,as1r2} <=> as2border1 \
                                           as2rr <=> as2leaf
                     as1r3 <=> as2border2 /

   - as1 to as2border links are eBGP sesssions
   - as2 to as2 links are iBGP sessions
   - All as1 routers originate and advertise 10.0.0.0/32 with distinct communities
   - as2border sets distinct communities for routes learned from each as1 device
   - All as2 routers have bgp multipath and add-path enabled
   - as2border routers set next-hop-self to respective as2rr routers
   - as2border1 should advertise 2 routes to as2rr
   - as2border2 should advertise 1 routes to as2rr
   - as2rr should have 2 ECMP-best routes and 1 backup route in its BGP RIB; and 2 in its main RIB
   - as2rr should advertise 1 route from as2border1 and 1 route from as2border2 to as2leaf
   - as2leaf should have 2 routes in its BGP RIB, and 2 routes in its main RIB
  */
  @Before
  public void setup() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/add-path/unique-next-hop";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    snapshotPath,
                    "as1r1",
                    "as1r2",
                    "as1r3",
                    "as2border1",
                    "as2border2",
                    "as2rr",
                    "as2leaf")
                .build(),
            _folder);
    // TODO: parse neighbor xxx additional-paths send receive
    batfish.getSettings().setDisableUnrecognized(false);
    batfish.computeDataPlane(batfish.getSnapshot());
    _dp = batfish.loadDataPlane(batfish.getSnapshot());
  }

  @Test
  public void testBgpRib() {
    Table<String, String, Set<Bgpv4Route>> bgpRoutes = _dp.getBgpRoutes();
    assertThat(bgpRoutes.get("as2border1", DEFAULT_VRF_NAME), hasSize(2));
    assertThat(bgpRoutes.get("as2border2", DEFAULT_VRF_NAME), hasSize(1));
    assertThat(bgpRoutes.get("as2rr", DEFAULT_VRF_NAME), hasSize(2));
    assertThat(_dp.getBgpBackupRoutes().get("as2rr", DEFAULT_VRF_NAME), hasSize(1));
    assertThat(
        bgpRoutes.get("as2leaf", DEFAULT_VRF_NAME),
        containsInAnyOrder(
            hasNextHop(NextHopIp.of(Ip.parse("10.0.2.1"))),
            hasNextHop(NextHopIp.of(Ip.parse("10.0.2.2")))));
  }

  @Test
  public void testMainRib() {
    Table<String, String, FinalMainRib> ribs = _dp.getRibs();
    assertThat(ribs.get("as2border1", DEFAULT_VRF_NAME).getRoutes(PREFIX), hasSize(2));
    assertThat(ribs.get("as2border2", DEFAULT_VRF_NAME).getRoutes(PREFIX), hasSize(1));
    assertThat(ribs.get("as2rr", DEFAULT_VRF_NAME).getRoutes(PREFIX), hasSize(2));
    assertThat(
        ribs.get("as2leaf", DEFAULT_VRF_NAME).getRoutes(PREFIX),
        containsInAnyOrder(
            hasNextHop(NextHopIp.of(Ip.parse("10.0.2.1"))),
            hasNextHop(NextHopIp.of(Ip.parse("10.0.2.2")))));
  }
}
