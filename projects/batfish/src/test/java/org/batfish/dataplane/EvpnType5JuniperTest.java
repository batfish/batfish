package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AnnotatedRouteMatchers.hasRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.ibdp.IncrementalDataPlane;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** E2E dataplane test for Juniper EVPN Type-5 (IP prefix routes) VRF leaking. */
public class EvpnType5JuniperTest {
  private static final String TESTCONFIGS_PATH = "org/batfish/dataplane/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testType5RoutePresence() throws IOException {
    /*
    Single-node test: ge-0/0/0.0 (10.1.0.1/24) is in vrf1.
    - vrf1: has EVPN ip-prefix-routes with VNI 1001, exports direct routes via vrf1-export policy,
      route-target target:65000:1001, route-distinguisher 10.0.0.1:1001
    - vrf2: has EVPN ip-prefix-routes with VNI 1002, imports route-target target:65000:1001,
      route-distinguisher 10.0.0.1:1002

    Expected: connected route 10.1.0.0/24 in vrf1 → redistributed into vrf1 BGP →
    leaked as EVPN Type-5 into default VRF → imported into vrf2 as BGPv4 route.
     */
    String hostname = "evpn-type5-juniper";
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(_folder, TESTCONFIGS_PATH + hostname);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    Prefix prefix = Prefix.parse("10.1.0.0/24"); // connected route in vrf1

    SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>> ribs =
        dp.getRibsForTesting().get(hostname);

    // The connected route should not appear in the default VRF's main RIB
    assertThat(ribs.get(DEFAULT_VRF_NAME).getRoutes(prefix), empty());

    // vrf1 should have the connected route
    assertThat(
        ribs.get("vrf1").getRoutes(prefix), contains(hasRoute(instanceOf(ConnectedRoute.class))));

    // vrf2 should have the route as a BGPv4 route (leaked via EVPN)
    assertThat(
        ribs.get("vrf2").getRoutes(prefix), contains(hasRoute(instanceOf(Bgpv4Route.class))));
  }
}
