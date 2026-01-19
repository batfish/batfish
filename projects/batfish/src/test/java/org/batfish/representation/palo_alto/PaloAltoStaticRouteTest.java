package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;

import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class PaloAltoStaticRouteTest {

  private PaloAltoConfiguration createConfigWithStaticRoute(
      String vrName, String routeName, Ip nextHopIp, Prefix destination) {
    PaloAltoConfiguration config = new PaloAltoConfiguration();
    config.setVendor(ConfigurationFormat.PALO_ALTO);
    config.setWarnings(new Warnings());
    config.setHostname("FW1");

    VirtualRouter vr = new VirtualRouter(vrName);
    config.getVirtualRouters().put(vrName, vr);

    StaticRoute route = new StaticRoute(routeName);
    route.setNextHopIp(nextHopIp);
    route.setDestination(destination);
    vr.getStaticRoutes().put(routeName, route);

    return config;
  }

  @Test
  public void testStaticRouteWithNextHopIp() {
    PaloAltoConfiguration config =
        createConfigWithStaticRoute(
            "VR1", "ROUTE-NAME", Ip.parse("10.10.10.10"), Prefix.parse("1.1.1.0/24"));

    VirtualRouter vr = config.getVirtualRouters().get("VR1");
    assertNotNull("VR1 should exist", vr);

    StaticRoute route = vr.getStaticRoutes().get("ROUTE-NAME");
    assertNotNull("ROUTE-NAME should exist", route);

    assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.10.10.10")));
    assertThat(route.getDestination(), equalTo(Prefix.parse("1.1.1.0/24")));

    org.batfish.datamodel.Configuration viConfig =
        config.toVendorIndependentConfigurations().get(0);
    org.batfish.datamodel.Vrf viVrf = viConfig.getVrfs().get("VR1");
    assertNotNull(viVrf);

    boolean found = false;
    for (org.batfish.datamodel.StaticRoute sr : viVrf.getStaticRoutes()) {
      if (sr.getNetwork().equals(Prefix.parse("1.1.1.0/24"))
          && sr.getNextHopIp().equals(Ip.parse("10.10.10.10"))) {
        found = true;
        break;
      }
    }

    assertThat("Should find converted static route with IP next hop", found);
  }

  @Test
  public void testStaticRouteWithNextHopIpUnmasked() {
    PaloAltoConfiguration config =
        createConfigWithStaticRoute(
            "VR1", "ROUTE-NAME", Ip.parse("10.10.10.10"), Prefix.parse("1.1.1.0/24"));

    org.batfish.datamodel.Configuration viConfig =
        config.toVendorIndependentConfigurations().get(0);
    org.batfish.datamodel.Vrf viVrf = viConfig.getVrfs().get("VR1");

    boolean found = false;
    for (org.batfish.datamodel.StaticRoute sr : viVrf.getStaticRoutes()) {
      if (sr.getNetwork().equals(Prefix.parse("1.1.1.0/24"))
          && sr.getNextHopIp().equals(Ip.parse("10.10.10.10"))) {
        found = true;
        break;
      }
    }
    assertThat("Should find converted static route with unmasked IP next hop", found);
  }
}
