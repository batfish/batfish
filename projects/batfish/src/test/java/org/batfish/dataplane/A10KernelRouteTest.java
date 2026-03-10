package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isBgpv4RouteThat;
import static org.batfish.vendor.a10.representation.A10Conversion.KERNEL_ROUTE_TAG_FLOATING_IP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.ibdp.IncrementalDataPlane;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of A10 kernel routes, vrrp-a, and BGP. */
public final class A10KernelRouteTest {
  private static final String SNAPSHOT_FOLDER = "org/batfish/dataplane/testrigs/a10-kernel-routes";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Ip FLOATING_IP = Ip.parse("10.0.1.10");
  private static final Prefix KERNEL_PREFIX = Prefix.create(FLOATING_IP, MAX_PREFIX_LENGTH);

  private Batfish _batfish;

  /*
   * Topology:
   *
   *          vrrp-a
   *    .1  10.0.2.0/24  .2
   *    ethernet2 ethernet2
   *    <=================>
   * r1                     r2
   *    <=================>
   *    ethernet1 ethernet1
   *    .1  10.0.1.0/24  .2
   *           bgp
   * - There is a BGP session between r1 and r2 on ethernet1
   * - There is vrrp-a between r1 and r2 on ethernet2
   * - r1 and r2 both have a floating-ip 10.0.1.10
   * - r1 and r2 redistribute floating IPs into BGP
   * - r1 has higher priority, so:
   *   - 10.0.1.10/32 should appear in r1's BGP RIB as a local route
   *   - 10.0.1.10/32 should appear in r2's BGP RIB as a received route route
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
  public void testMainRibRoutes() {
    IncrementalDataPlane dp = (IncrementalDataPlane) _batfish.loadDataPlane(_batfish.getSnapshot());

    assertThat(
        dp.getRibsForTesting().get("r1").get(DEFAULT_VRF_NAME).getRoutes(KERNEL_PREFIX).stream()
            .map(AnnotatedRoute::getAbstractRoute)
            .collect(ImmutableSet.toImmutableSet()),
        contains(
            KernelRoute.builder()
                .setTag(KERNEL_ROUTE_TAG_FLOATING_IP)
                .setRequiredOwnedIp(FLOATING_IP)
                .setNetwork(KERNEL_PREFIX)
                .build()));
    assertThat(
        dp.getRibsForTesting().get("r2").get(DEFAULT_VRF_NAME).getRoutes(KERNEL_PREFIX).stream()
            .map(AnnotatedRoute::getAbstractRoute)
            .collect(ImmutableSet.toImmutableSet()),
        contains(isBgpv4RouteThat(allOf(hasAsPath(equalTo(AsPath.ofSingletonAsSets(1L)))))));
  }

  @Test
  public void testBgpRibRoutes() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());

    assertThat(
        dp.getBgpRoutes().get("r1", DEFAULT_VRF_NAME),
        containsInAnyOrder(
            isBgpv4RouteThat(allOf(hasPrefix(KERNEL_PREFIX), hasAsPath(equalTo(AsPath.empty()))))));
    assertThat(
        dp.getBgpRoutes().get("r2", DEFAULT_VRF_NAME),
        containsInAnyOrder(
            isBgpv4RouteThat(
                allOf(
                    hasPrefix(KERNEL_PREFIX), hasAsPath(equalTo(AsPath.ofSingletonAsSets(1L)))))));
  }
}
