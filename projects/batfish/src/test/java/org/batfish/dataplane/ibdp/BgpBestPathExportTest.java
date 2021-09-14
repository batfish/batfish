package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of BGP best-path export logic. */
public final class BgpBestPathExportTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /*
   * Topology: {R1,R2} <=> R3 <=> R4 <=> R5
   * All routers have BGP multipath enabled
   * R5 has static routes to R1/2's subnets
   * In round 1:
   * - R1 and R2 redistribute routes for 10.1.0.0/24 to R3
   * - The resulting BGP RIB routes on R3 are ECMP-equivalent
   * - R4 redistributes static routes for R3/5's loopbacks to R5/3
   * In round 2:
   * - R3 and R5 start an iBGP session
   * - R3 should only export a single best route for 10.1.0.0/24 to R5
   */
  @Test
  public void testOnlyBestPathsExportedNewSession() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-best-path-export/new-session";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "r1", "r2", "r3", "r4", "r5")
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    Set<Bgpv4Route> bgpRoutesOnR3 =
        dataplane.getBgpRoutes().get("r3", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR4 =
        dataplane.getBgpRoutes().get("r4", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR5 =
        dataplane.getBgpRoutes().get("r5", Configuration.DEFAULT_VRF_NAME);
    assertThat(
        bgpRoutesOnR3.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should receive from R4
            hasPrefix(Prefix.parse("5.5.5.5/32")),
            // should have two paths for 10.1.0.0/24; one from R1, and one from R2
            hasPrefix(Prefix.parse("10.1.0.0/24")),
            hasPrefix(Prefix.parse("10.1.0.0/24"))));
    assertThat(
        bgpRoutesOnR4.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should be redistributing these
            hasPrefix(Prefix.parse("3.3.3.3/32")), hasPrefix(Prefix.parse("5.5.5.5/32"))));
    assertThat(
        bgpRoutesOnR5.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should receive from R4
            hasPrefix(Prefix.parse("3.3.3.3/32")),
            hasPrefix(
                // should only have received single best-path entry for 10.1.0.0/24 from R3
                Prefix.parse("10.1.0.0/24"))));
  }

  /*
   * Topology: {R1,R2} <=> R3 <=> R4
   * All routers have BGP multipath enabled
   * R4 has static routes to R1/2's subnets
   * R3 and R4 have static routes to each other's loopback
   * In round 1:
   * - R1 and R2 redistribute routes for 10.1.0.0/24 to R3
   * - The resulting BGP RIB routes on R3 are ECMP-equivalent
   * - R3 and R4 start an iBGP session
   * In round 2:
   * - R3 should only export a single best route for 10.1.0.0/24 to R5
   */
  @Test
  public void testOnlyBestPathsExportedOldSession() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-best-path-export/old-session";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "r1", "r2", "r3", "r4")
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    Set<Bgpv4Route> bgpRoutesOnR3 =
        dataplane.getBgpRoutes().get("r3", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR4 =
        dataplane.getBgpRoutes().get("r4", Configuration.DEFAULT_VRF_NAME);
    assertThat(
        bgpRoutesOnR3.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should have two paths for 10.1.0.0/24; one from R1, and one from R2
            hasPrefix(Prefix.parse("10.1.0.0/24")), hasPrefix(Prefix.parse("10.1.0.0/24"))));
    assertThat(
        bgpRoutesOnR4.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            hasPrefix(
                // should only have received single best-path entry for 10.1.0.0/24 from R3
                Prefix.parse("10.1.0.0/24"))));
  }

  /*
   * Topology: R1 <=> {R2,R3} <=> R4 <=> R5
   * All routers have BGP multipath enabled
   * R1-4 have static routes to each other's loopbacks
   * R1-3 have static routes for 10.1.0.0/24
   * R2 and R3 have higher than BGP admin distance for their 10.1.0.0/24 static route
   * R2 and R3 redistribute 10.1.0.0/24 with weight 0
   * R2 and R3 are route reflectors
   * In round 1:
   * - R1 starts iBGP sessions with R2 and R3
   * - R3 starts iBGP sessions with R2 and R3
   * - R1 redistributes 10.1.0.0/24 to R2 and R3 with high local preference
   * - R2 and R3 redistribute 10.1.0.0/24 to R4 with low local preference
   * - R4 starts eBGP session with R5
   * In round 2:
   * - R4 exports low local preference 10.1.0.0/24 to R5, erasing local preference and replacing NH
   * - R2 and R3 export high local preference 10.1.0.0/24 received from R1 to R4
   * - R4's resulting BGP RIB delta should have WITHDRAW of single best route from R2 or R3,
   *   and ADD of new best route originating from R1
   * In round 3:
   * - R4's earlier BGP RIB for 10.1.0.0/24 should result in no-OP export delta, since transform
   *   of old best route and new best route should be identical.
   */
  @Test
  public void testOnlyPreviousBestPathsWithdrawn() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/bgp-best-path-export/withdrawal";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "r1", "r2", "r3", "r4", "r5")
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    Set<Bgpv4Route> bgpRoutesOnR2 =
        dataplane.getBgpRoutes().get("r2", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR3 =
        dataplane.getBgpRoutes().get("r3", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR4 =
        dataplane.getBgpRoutes().get("r4", Configuration.DEFAULT_VRF_NAME);
    Set<Bgpv4Route> bgpRoutesOnR5 =
        dataplane.getBgpRoutes().get("r5", Configuration.DEFAULT_VRF_NAME);
    assertThat(
        bgpRoutesOnR2.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should have single best path for 10.1.0.0/24 through R1
            allOf(
                hasPrefix(Prefix.parse("10.1.0.0/24")),
                hasNextHop(NextHopIp.of(Ip.parse("1.1.1.1"))))));
    assertThat(
        bgpRoutesOnR3.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should have single best path for 10.1.0.0/24 through R1
            allOf(
                hasPrefix(Prefix.parse("10.1.0.0/24")),
                hasNextHop(NextHopIp.of(Ip.parse("1.1.1.1"))))));
    assertThat(
        bgpRoutesOnR4.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            // should have two paths for 10.1.0.0/24; one through R2, and one through R3
            // both should have next hop of R1
            allOf(
                hasPrefix(Prefix.parse("10.1.0.0/24")),
                hasNextHop(NextHopIp.of(Ip.parse("1.1.1.1")))),
            allOf(
                hasPrefix(Prefix.parse("10.1.0.0/24")),
                hasNextHop(NextHopIp.of(Ip.parse("1.1.1.1"))))));
    assertThat(
        bgpRoutesOnR5.stream().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            hasPrefix(
                // should only have received single best-path entry for 10.1.0.0/24 from R4
                Prefix.parse("10.1.0.0/24"))));
  }
}
