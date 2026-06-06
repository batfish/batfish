package org.batfish.dataplane.ibdp;

import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;

import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests that advertise-inactive (and its suppression) takes effect on iBGP sessions, not just eBGP.
 *
 * <p>as1 (AS 1) originates a default route over eBGP to as2 (AS 2). as2 has a static default route
 * that preempts the eBGP-learned default, leaving the BGP route inactive in the main RIB. as2
 * advertises to as3 (AS 2) over an iBGP session. The inactive route reaches as3 only when as2
 * advertises inactive routes, which is the IOS default and is disabled by "bgp suppress-inactive".
 */
public final class BgpFilterInactiveIbgpTest {

  private static final String AS1_NAME = "as1";
  private static final String AS2_ADVERTISE_INACTIVE_NAME = "as2-advertise-inactive";
  private static final String AS2_SUPPRESS_INACTIVE_NAME = "as2-suppress-inactive";
  private static final String AS3_NAME = "as3";
  private static final String SNAPSHOT_PATH = "org/batfish/dataplane/ibdp/bgp-filter-inactive-ibgp";
  private static final Ip STATIC_NEXT_HOP = Ip.parse("10.0.12.254");

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testSuppressInactive() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    SNAPSHOT_PATH, AS1_NAME, AS2_SUPPRESS_INACTIVE_NAME, AS3_NAME)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    assertRoute(routes, RoutingProtocol.STATIC, AS1_NAME, Prefix.ZERO, 0, STATIC_NEXT_HOP);
    assertRoute(
        routes,
        RoutingProtocol.STATIC,
        AS2_SUPPRESS_INACTIVE_NAME,
        Prefix.ZERO,
        0,
        STATIC_NEXT_HOP);
    // as2 suppresses inactive routes, so the inactive default is not advertised over iBGP.
    assertNoRoute(routes, AS3_NAME, Prefix.ZERO);
  }

  @Test
  public void testAdvertiseInactive() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    SNAPSHOT_PATH, AS1_NAME, AS2_ADVERTISE_INACTIVE_NAME, AS3_NAME)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    assertRoute(routes, RoutingProtocol.STATIC, AS1_NAME, Prefix.ZERO, 0, STATIC_NEXT_HOP);
    assertRoute(
        routes,
        RoutingProtocol.STATIC,
        AS2_ADVERTISE_INACTIVE_NAME,
        Prefix.ZERO,
        0,
        STATIC_NEXT_HOP);
    // as2 advertises inactive routes by default, so as3 learns the default over iBGP.
    assertRoute(routes, RoutingProtocol.IBGP, AS3_NAME, Prefix.ZERO, 0, Ip.parse("10.0.23.2"));
  }
}
