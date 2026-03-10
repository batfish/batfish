package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests that a router that sends all additional-paths will advertise routes that are not ECMP-best,
 * and that both will be present in the BGP RIB on a receiver.
 */
public final class BgpAddPathNonEcmpBestTest {

  private static final Prefix PREFIX = Prefix.strict("10.0.0.0/32");
  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();
  private DataPlane _dp;

  /*
   Topology: {as1r1,as1r2} <=> as2border <=> as2leaf

   - as1 to as2border links are eBGP sesssions
   - as2border to as2leaf link is iBGP session
   - Both as1 routers originate and advertise 10.0.0.0/32
   - as2border sets distinct communities and local pref for routes learned from each as1
   - Both as2 routers have bgp multipath and add-path enabled
   - as2border preserves external next hop when advertising to as2leaf
   - as2border should have 1 best and 1 backup route in its BGP RIB; and 1 route in its main RIB
   - as2border should advertise 2 routes to as2leaf
   - as2leaf should have 1 best and 1 backup route in its BGP RIB; and 1 routes in its main RIB
  */
  @Before
  public void setup() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/add-path/non-ecmp-best";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "as1r1", "as1r2", "as2border", "as2leaf")
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
    Table<String, String, Set<Bgpv4Route>> backupRoutes = _dp.getBgpBackupRoutes();

    // as2border
    assertThat(
        bgpRoutes.get("as2border", DEFAULT_VRF_NAME),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.2.1")))));
    assertThat(
        backupRoutes.get("as2border", DEFAULT_VRF_NAME),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.1.1")))));

    // as2leaf
    assertThat(
        bgpRoutes.get("as2leaf", DEFAULT_VRF_NAME),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.2.1")))));
    // TODO: with add-path, advertise non-ecmp-best routes
    _thrown.expect(AssertionError.class);
    assertThat(
        backupRoutes.get("as2leaf", DEFAULT_VRF_NAME),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.1.1")))));
  }

  @Test
  public void testMainRib() {
    Table<String, String, FinalMainRib> ribs = _dp.getRibs();

    // as2border
    assertThat(
        ribs.get("as2border", DEFAULT_VRF_NAME).getRoutes(PREFIX),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.2.1")))));

    // as2leaf
    assertThat(
        ribs.get("as2leaf", DEFAULT_VRF_NAME).getRoutes(PREFIX),
        contains(hasNextHop(NextHopIp.of(Ip.parse("10.12.2.1")))));
  }
}
