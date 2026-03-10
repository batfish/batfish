package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isReceivedFromRouteReflectorClient;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of the setting of the receivedFromRouteReflectorClient BGP route bit. */
public final class ReceivedFromRouteReflectorTest {

  /*
  rrcm: Route-Reflector-Client that exports from the Main RIB
  nrrcm: Non-Route-Reflector-Client that exports from the Main RIB
  rrcb: Route-Reflector-Client that exports from the BGP RIB
  nrrcb: Non-Route-Reflector-Client that exports from the BGP RIB
  rr: Route-Reflector
  AS2: AS of all the devices above
  AS1: source of external eBGP advertisements

                     AS2       AS2
   Topology: AS1 <=> rrcm  <=> rr
                 <=> nrrcm <=>
                 <=> rrcb  <=>
                 <=> nrrcb <=>
   - rr has iBGP peerings with each of rrcm, nrrcm, rrcb, nrrcb
   - rr considers rrcm and rrcb to be route-reflector-clients
   - rr does not consider nrrcm nor nrrcb to be route-reflector-clients
   - rrcm and nrrcm export from the main RIB (Juniper)
   - rrcb and nrrcb export from the BGP RIB (Cisco IOS)
   - rrcm, nrrcm, rrcb, and nrrcb each originate a distinct /32 subnet of distinct /31 supernets
   - rrcm, nrrcm, rrcb, and nrrcb each originate an aggregate /31 supernet of their respective /32 above
   - rrcm, nrrcm, rrcb, and nrrcb each receive a distinct eBGP /32 from AS1 via external BGP announcements
   - all routes rr receives from rrcm and rrcb should have the receivedFromRouteReflectorClient bit
     set in rr's main/BGP RIBs
   - all routes rr receives from nrrcm and nrrcb should have the receivedFromRouteReflectorClient
     bit unset in rr's main/BGP RIBs
  */
  @Before
  public void setup() throws IOException {
    String snapshotPath = "org/batfish/dataplane/ibdp/route-reflection/rrrc";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(snapshotPath, "rrcm", "nrrcm", "rrcb", "nrrcb", "rr")
                .setExternalBgpAnnouncements(snapshotPath)
                .build(),
            _folder);
    batfish.getSettings().setDisableUnrecognized(true);
    batfish.computeDataPlane(batfish.getSnapshot());
    _dp = batfish.loadDataPlane(batfish.getSnapshot());
  }

  private static final Prefix NRRCB_STATIC = Prefix.strict("2.0.0.0/32");
  private static final Prefix NRRCB_AGGREGATE = Prefix.strict("2.0.0.0/31");
  private static final Prefix NRRCB_EBGP = Prefix.strict("1.0.0.2/32");
  private static final Prefix NRRCM_STATIC = Prefix.strict("3.0.0.0/32");
  private static final Prefix NRRCM_AGGREGATE = Prefix.strict("3.0.0.0/31");
  private static final Prefix NRRCM_EBGP = Prefix.strict("1.0.0.3/32");
  private static final Prefix RRCB_STATIC = Prefix.strict("4.0.0.0/32");
  private static final Prefix RRCB_AGGREGATE = Prefix.strict("4.0.0.0/31");
  private static final Prefix RRCB_EBGP = Prefix.strict("1.0.0.4/32");
  private static final Prefix RRCM_STATIC = Prefix.strict("5.0.0.0/32");
  private static final Prefix RRCM_AGGREGATE = Prefix.strict("5.0.0.0/31");
  private static final Prefix RRCM_EBGP = Prefix.strict("1.0.0.5/32");

  @Test
  public void testBgpRib() {
    Table<String, String, Set<Bgpv4Route>> bgpRoutes = _dp.getBgpRoutes();
    Set<Bgpv4Route> rrRoutes = bgpRoutes.get("rr", DEFAULT_VRF_NAME);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCB_STATIC);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCB_AGGREGATE);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCB_EBGP);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCM_STATIC);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCM_AGGREGATE);
    assertNotReceivedFromRouteReflectorClient(rrRoutes, NRRCM_EBGP);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCB_STATIC);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCB_AGGREGATE);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCB_EBGP);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCM_STATIC);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCM_AGGREGATE);
    assertReceivedFromRouteReflectorClient(rrRoutes, RRCM_EBGP);
    assertThat(rrRoutes, hasSize(12));
  }

  /**
   * Assert that the route with given {@code prefix} exists in {@code routes}, and that its
   * receivedFromRouteReflectorClient bit is set.
   */
  private static void assertReceivedFromRouteReflectorClient(
      Set<Bgpv4Route> routes, Prefix prefix) {
    assertThat(routes, hasItem(allOf(hasPrefix(prefix), isReceivedFromRouteReflectorClient())));
  }

  /**
   * Assert that the route with given {@code prefix} exists in {@code routes}, and that its
   * receivedFromRouteReflectorClient bit is unset.
   */
  private static void assertNotReceivedFromRouteReflectorClient(
      Set<Bgpv4Route> routes, Prefix prefix) {
    assertThat(
        routes, hasItem(allOf(hasPrefix(prefix), isReceivedFromRouteReflectorClient(false))));
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private DataPlane _dp;
}
