package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.junit.Test;

public class FortiosBgpConversionsTest {
  @Test
  public void testGetVrfs_singleNeighborSingleVrf() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface iface1 =
        Interface.builder()
            .setName("port1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("192.168.1.1"), 24))
            .build();
    iface1.setVrfName("default");
    c.getAllInterfaces().put("port1", iface1);

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setAs(65001L);

    BgpNeighbor neighbor = new BgpNeighbor(Ip.parse("10.0.0.1"));
    neighbor.setUpdateSource("port1");
    bgpProcess.getNeighbors().put(Ip.parse("10.0.0.1"), neighbor);

    Warnings warnings = new Warnings(true, true, true);

    var vrfs = FortiosBgpConversions.getVrfs(bgpProcess, c, warnings);

    assertThat(vrfs.size(), equalTo(1));
    assertThat(vrfs, hasItem("default"));
  }

  @Test
  public void testGetVrfs_neighborWithoutUpdateSource() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface iface1 =
        Interface.builder()
            .setName("port1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.1"), 24))
            .build();
    iface1.setVrfName("default");
    c.getAllInterfaces().put("port1", iface1);

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setAs(65001L);

    BgpNeighbor neighbor = new BgpNeighbor(Ip.parse("10.0.0.1"));
    // No update source set, should infer from interface with matching IP
    bgpProcess.getNeighbors().put(Ip.parse("10.0.0.1"), neighbor);

    Warnings warnings = new Warnings(true, true, true);

    var vrfs = FortiosBgpConversions.getVrfs(bgpProcess, c, warnings);

    assertThat(vrfs.size(), equalTo(1));
    assertThat(vrfs, hasItem("default"));
  }

  @Test
  public void testConvertBgp_disabledProcess() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setAs(0L); // AS 0 means disabled

    Warnings warnings = new Warnings(true, true, true);

    FortiosBgpConversions.convertBgp(bgpProcess, c, warnings);

    // Should complete without error and without creating BGP process
    assertNull("Should not have BGP process", c.getVrfs().get("default").getBgpProcess());
  }

  @Test
  public void testConvertBgp_prohibitedAsNumber() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setAs(65535L); // RFC 7300 prohibited

    Warnings warnings = new Warnings(true, true, true);

    FortiosBgpConversions.convertBgp(bgpProcess, c, warnings);

    // Should complete without error and without creating BGP process
    assertNull("Should not have BGP process", c.getVrfs().get("default").getBgpProcess());
    assertFalse("Should have warning about prohibited AS", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertBgp_noRouterId() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setAs(65001L);
    bgpProcess.setRouterId(null); // No router ID

    Warnings warnings = new Warnings(true, true, true);

    FortiosBgpConversions.convertBgp(bgpProcess, c, warnings);

    // Should complete without error and without creating BGP process
    assertNull("Should not have BGP process", c.getVrfs().get("default").getBgpProcess());
    assertFalse(
        "Should have warning about missing router ID", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertRouteMap_simplePermitRule() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);

    RouteMap routeMap = new RouteMap("test-routemap");
    RouteMapRule rule = new RouteMapRule("1");
    rule.setAction(RouteMapRule.Action.PERMIT);
    routeMap.setRules(ImmutableMap.of("1", rule));

    Warnings warnings = new Warnings(true, true, true);
    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    assertTrue("Should have routing policy", c.getRoutingPolicies().containsKey("test-routemap"));
    RoutingPolicy policy = c.getRoutingPolicies().get("test-routemap");
    assertThat(policy.getStatements().size(), equalTo(2)); // rule + default deny
  }

  @Test
  public void testConvertRouteMap_withMatchIp() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    c.getRouteFilterLists()
        .put("test_prefix_list", new org.batfish.datamodel.RouteFilterList("test_prefix_list"));

    RouteMap routeMap = new RouteMap("test-routemap");
    RouteMapRule rule = new RouteMapRule("1");
    rule.setAction(RouteMapRule.Action.PERMIT);
    rule.setMatchIpAddress("test_prefix_list");
    routeMap.setRules(ImmutableMap.of("1", rule));

    Warnings warnings = new Warnings(true, true, true);
    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    assertTrue("Should have routing policy", c.getRoutingPolicies().containsKey("test-routemap"));
    RoutingPolicy policy = c.getRoutingPolicies().get("test-routemap");
    assertThat(policy.getStatements().size(), equalTo(2)); // rule + default deny
  }

  @Test
  public void testConvertRouteMap_withNonExistentList() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);

    RouteMap routeMap = new RouteMap("test-routemap");
    RouteMapRule rule = new RouteMapRule("1");
    rule.setAction(RouteMapRule.Action.PERMIT);
    rule.setMatchIpAddress("nonexistent_list");
    routeMap.setRules(ImmutableMap.of("1", rule));

    Warnings warnings = new Warnings(true, true, true);

    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    // Should complete but with warning about nonexistent list
    assertFalse("Should have warning", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertRouteMap_denyRule() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);

    RouteMap routeMap = new RouteMap("test-routemap");
    RouteMapRule rule = new RouteMapRule("1");
    rule.setAction(RouteMapRule.Action.DENY);
    routeMap.setRules(ImmutableMap.of("1", rule));

    Warnings warnings = new Warnings(true, true, true);
    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    assertTrue("Should have routing policy", c.getRoutingPolicies().containsKey("test-routemap"));
    RoutingPolicy policy = c.getRoutingPolicies().get("test-routemap");
    assertThat(policy.getStatements().size(), equalTo(2)); // rule + default deny
  }

  @Test
  public void testConvertRouteMap_emptyRules() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);

    RouteMap routeMap = new RouteMap("test-routemap");
    routeMap.setRules(ImmutableMap.of());

    Warnings warnings = new Warnings(true, true, true);
    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    assertTrue("Should have routing policy", c.getRoutingPolicies().containsKey("test-routemap"));
    RoutingPolicy policy = c.getRoutingPolicies().get("test-routemap");
    // Should only have default deny
    assertThat(policy.getStatements().size(), equalTo(1));
  }

  @Test
  public void testConvertRouteMap_multipleRules() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);

    RouteMap routeMap = new RouteMap("test-routemap");
    RouteMapRule rule1 = new RouteMapRule("1");
    rule1.setAction(RouteMapRule.Action.PERMIT);

    RouteMapRule rule2 = new RouteMapRule("2");
    rule2.setAction(RouteMapRule.Action.DENY);

    routeMap.setRules(ImmutableMap.of("1", rule1, "2", rule2));

    Warnings warnings = new Warnings(true, true, true);
    FortiosBgpConversions.convertRouteMap(routeMap, c, warnings);

    assertTrue("Should have routing policy", c.getRoutingPolicies().containsKey("test-routemap"));
    RoutingPolicy policy = c.getRoutingPolicies().get("test-routemap");
    // Should have 2 rules + default deny = 3 statements
    assertThat(policy.getStatements().size(), equalTo(3));
  }
}
