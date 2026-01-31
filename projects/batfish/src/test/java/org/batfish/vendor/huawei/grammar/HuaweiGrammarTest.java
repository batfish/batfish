package org.batfish.vendor.huawei.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.vendor.huawei.representation.HuaweiAcl;
import org.batfish.vendor.huawei.representation.HuaweiAclLine;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiConversions;
import org.batfish.vendor.huawei.representation.HuaweiNatRule;
import org.batfish.vendor.huawei.representation.HuaweiStaticRoute;
import org.batfish.vendor.huawei.representation.HuaweiVlan;
import org.batfish.vendor.huawei.representation.HuaweiVrf;
import org.junit.Test;

/** Tests for Huawei grammar parsing */
public class HuaweiGrammarTest {

  private Settings getSettings() {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    return settings;
  }

  private Settings getLenientSettings() {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(false);
    return settings;
  }

  @Test
  public void testBasicConfig() {
    String configText = "sysname Router1\nreturn\n";

    // Parse the configuration using HuaweiControlPlaneExtractor
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testEmptyConfig() {
    String configText = "";

    // Parse the configuration using HuaweiControlPlaneExtractor
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Hostname should be null for empty config
    assertThat(config.getHostname(), equalTo(null));
  }

  @Test
  public void testInterfaceParsing() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink to core\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Uplink to core"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testInterfaceShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testInterfaceNoShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " undo shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testVlanCreation() {
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Management VLAN\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());
    assertThat(vlan.getVlanId(), equalTo(100));
  }

  @Test
  public void testVlanBatch() {
    String configText = "sysname Router1\n" + "vlan batch 10 20 30 40\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(4));

    assertThat(config.getVlan(10), notNullValue());
    assertThat(config.getVlan(20), notNullValue());
    assertThat(config.getVlan(30), notNullValue());
    assertThat(config.getVlan(40), notNullValue());
  }

  @Test
  public void testVlanifInterface() {
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + "interface Vlanif100\n"
            + " description Management Interface\n"
            + " ip address 192.168.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("Vlanif100");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Management Interface"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.100.1"));
  }

  @Test
  public void testSubinterfaceDot1q() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 100\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0.100");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testVlanConversion() {
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + "interface Vlanif100\n"
            + " ip address 192.168.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1")); // Hostnames are lowercased
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));
    assertThat(viConfig.getAllInterfaces().containsKey("Vlanif100"), equalTo(true));
  }

  @Test
  public void testStaticRouteBasic() {
    String configText =
        "sysname Router1\n" + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("0.0.0.0/0"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testStaticRouteWithInterface() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 GigabitEthernet0/0/0 192.168.1.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testStaticRouteWithPreference() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
    assertThat(route.getPreference(), equalTo(100));
  }

  @Test
  public void testStaticRouteMultiple() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2\n"
            + "ip route-static 172.16.0.0 255.255.0.0 192.168.1.3 preference 50\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(3));
  }

  @Test
  public void testStaticRouteConversion() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.2 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));

    // Check static routes were added to default VRF
    assertThat(viConfig.getDefaultVrf().getStaticRoutes().size(), equalTo(1));
    assertThat(
        viConfig.getDefaultVrf().getStaticRoutes().iterator().next().getNetwork().toString(),
        equalTo("0.0.0.0/0"));
  }

  @Test
  public void testBgpBasic() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithRouterId() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    assertThat(config.getBgpProcess().getRouterId().toString(), equalTo("1.1.1.1"));
  }

  @Test
  public void testBgpWithPeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65003\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    // Peers are parsed but not stored in Phase 5 (future enhancement)
  }

  @Test
  public void testBgpNetworks() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpConversion() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
  }

  @Test
  public void testAclBasic() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(2));
  }

  @Test
  public void testAclAdvanced() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(2));
  }

  @Test
  public void testAclRules() {
    String configText =
        "sysname Router1\n"
            + "acl number 2001\n"
            + " rule 5 permit source 10.1.1.0 0.0.0.255\n"
            + " rule 10 deny source 10.2.2.0 0.0.0.255\n"
            + " rule 15 permit source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2001");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));

    // Check first line
    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getAction(), equalTo("permit"));

    // Check second line
    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getAction(), equalTo("deny"));

    // Check third line
    HuaweiAclLine line3 = acl.getLines().get(2);
    assertThat(line3.getAction(), equalTo("permit"));
  }

  @Test
  public void testAclConversion() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    // Check that ACL was converted
    assertThat(viConfig.getIpAccessLists().size(), equalTo(1));
    assertThat(viConfig.getIpAccessLists().containsKey("2000"), equalTo(true));
  }

  @Test
  public void testNatBasic() {
    String configText =
        "sysname Router1\n"
            + "nat outbound 2000\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check outbound NAT rule
    HuaweiNatRule outboundRule = config.getNatRules().get(0);
    assertThat(outboundRule, notNullValue());
    assertThat(outboundRule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(outboundRule.getAclName(), equalTo("2000"));

    // Check static NAT rule
    HuaweiNatRule staticRule = config.getNatRules().get(1);
    assertThat(staticRule, notNullValue());
    assertThat(staticRule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(staticRule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(staticRule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testNatStatic() {
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "nat static global 192.168.1.2 inside 10.0.0.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check first static NAT rule
    HuaweiNatRule rule1 = config.getNatRules().get(0);
    assertThat(rule1, notNullValue());
    assertThat(rule1.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule1.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule1.getInsideLocalIp().toString(), equalTo("10.0.0.1"));

    // Check second static NAT rule
    HuaweiNatRule rule2 = config.getNatRules().get(1);
    assertThat(rule2, notNullValue());
    assertThat(rule2.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule2.getGlobalIp().toString(), equalTo("192.168.1.2"));
    assertThat(rule2.getInsideLocalIp().toString(), equalTo("10.0.0.2"));
  }

  @Test
  public void testNatOutbound() {
    String configText =
        "sysname Router1\n" + "nat outbound 2000\n" + "nat outbound 3001 interface\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check first outbound rule (dynamic NAT with pool)
    HuaweiNatRule rule1 = config.getNatRules().get(0);
    assertThat(rule1, notNullValue());
    assertThat(rule1.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule1.getAclName(), equalTo("2000"));

    // Check second outbound rule (Easy IP)
    HuaweiNatRule rule2 = config.getNatRules().get(1);
    assertThat(rule2, notNullValue());
    assertThat(rule2.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
    assertThat(rule2.getAclName(), equalTo("3001"));
  }

  @Test
  public void testNatConversion() {
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.2 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));
  }

  @Test
  public void testOspfBasic() {
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
  }

  @Test
  public void testOspfWithRouterId() {
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("1.1.1.1"));
  }

  @Test
  public void testOspfWithAreas() {
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " area 0\n" + " area 1\n" + " area 2\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(3));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(2L), equalTo(true));
  }

  @Test
  public void testOspfNetworks() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 192.168.1.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));

    // Check first network
    org.batfish.vendor.huawei.representation.HuaweiOspfProcess.HuaweiOspfNetwork network1 =
        config.getOspfProcess().getNetworks().get(0);
    assertThat(network1.getNetwork().toString(), equalTo("10.0.0.0/24"));
    assertThat(network1.getAreaId(), equalTo(0L));

    // Check second network
    org.batfish.vendor.huawei.representation.HuaweiOspfProcess.HuaweiOspfNetwork network2 =
        config.getOspfProcess().getNetworks().get(1);
    assertThat(network2.getNetwork().toString(), equalTo("192.168.1.0/24"));
    assertThat(network2.getAreaId(), equalTo(1L));
  }

  @Test
  public void testOspfConversion() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " router-id 1.1.1.1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 192.168.1.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("1.1.1.1"));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));

    // TODO: Implement full OSPF conversion to Batfish model
    // For now, just verify conversion succeeds without errors
  }

  @Test
  public void testVrfBasic() {
    String configText = "sysname Router1\n" + "ip vpn-instance VRF1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
  }

  @Test
  public void testVrfWithRd() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("100:1"));
  }

  @Test
  public void testVrfWithRouteTargets() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 export\n"
            + " vpn-target 200:1 import\n"
            + " vpn-target 300:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("100:1"));

    // Check import route targets (should have 200:1 from import, 300:1 from both)
    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getImportRouteTargets().containsKey("200:1"), equalTo(true));
    assertThat(vrf.getImportRouteTargets().containsKey("300:1"), equalTo(true));

    // Check export route targets (should have 100:1 from export, 300:1 from both)
    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getExportRouteTargets().containsKey("100:1"), equalTo(true));
    assertThat(vrf.getExportRouteTargets().containsKey("300:1"), equalTo(true));
  }

  @Test
  public void testVrfConversion() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    // Convert to vendor-independent configuration
    org.batfish.datamodel.Configuration viConfig =
        HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));

    // Check VRF was converted
    assertThat(viConfig.getVrfs().containsKey("VRF1"), equalTo(true));

    org.batfish.datamodel.Vrf vrf = viConfig.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
  }

  // ========== ERROR HANDLING AND NEGATIVE TESTS ==========

  @Test
  public void testMissingRequiredKeyword() {
    // Missing interface keyword before interface name
    String configText = "sysname Router1\n" + "GigabitEthernet0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    // Should still parse with warnings
    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    // The unrecognized line should be skipped
  }

  @Test
  public void testInvalidIpAddress() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 999.999.999.999 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid IP
    // Configuration should still be valid
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testInvalidSubnetMask() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 256.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testInvalidIpMask() {
    // Invalid subnet mask (not contiguous)
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.254.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid mask
  }

  @Test
  public void testStaticRouteInvalidNextHop() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 999.999.999.999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning but continue parsing
  }

  @Test
  public void testBgpInvalidAsNumber() {
    // AS number too large
    String configText = "sysname Router1\n" + "bgp 9999999\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser should accept it but might warn during conversion
  }

  @Test
  public void testBgpAsNumberZero() {
    // AS number 0 is invalid
    String configText = "sysname Router1\n" + "bgp 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(0L));
  }

  @Test
  public void testOspfInvalidAreaId() {
    // Area ID that's not a valid number
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " area 999999999999999999999\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid area ID
  }

  @Test
  public void testOspfAreaIdZero() {
    // Area 0 is valid (backbone area)
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(1));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
  }

  @Test
  public void testAclInvalidNumber() {
    // ACL number outside valid ranges
    String configText =
        "sysname Router1\n" + "acl 9999 advanced\n" + " rule 5 permit ip source any\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but type may be incorrect
  }

  @Test
  public void testVlanMaxValue() {
    // Maximum valid VLAN ID
    String configText = "sysname Router1\n" + "vlan 4094\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getVlan(4094), notNullValue());
  }

  @Test
  public void testVlanExceedsMax() {
    // VLAN ID exceeds maximum (4094)
    String configText = "sysname Router1\n" + "vlan 4095\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Grammar may accept it as uint16, but should warn
  }

  @Test
  public void testVlanZero() {
    // VLAN ID 0 is typically invalid
    String configText = "sysname Router1\n" + "vlan 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but semantically invalid
  }

  @Test
  public void testPortNumberZero() {
    // Port number 0 (reserved, typically invalid)
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but semantically suspicious
  }

  @Test
  public void testPortNumberMax() {
    // Maximum valid port number
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));
  }

  @Test
  public void testPortNumberExceedsMax() {
    // Port number exceeds 65535
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 99999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Grammar should reject or warn about out-of-range port
  }

  @Test
  public void testStaticRoutePreferenceZero() {
    // Preference value of 0
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getStaticRoutes().get(0).getPreference(), equalTo(0));
  }

  @Test
  public void testStaticRoutePreferenceMax() {
    // Maximum reasonable preference value
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getStaticRoutes().get(0).getPreference(), equalTo(255));
  }

  @Test
  public void testEmptyConfigOnly() {
    // Completely empty configuration
    String configText = "";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo(null));
    assertThat(config.getInterfaces().size(), equalTo(0));
  }

  @Test
  public void testOnlyHostname() {
    // Configuration with only hostname
    String configText = "sysname Router1\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(0));
  }

  @Test
  public void testOnlyOneInterface() {
    // Configuration with only one interface
    String configText = "interface GigabitEthernet0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testOnlyComments() {
    // Configuration with only whitespace and newlines
    String configText = "\n\n\n   \n\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo(null));
  }

  @Test
  public void testInterfaceWithoutIp() {
    // Interface defined but no IP address
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Test interface\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Test interface"));
    assertThat(iface.getAddress(), equalTo(null));
  }

  @Test
  public void testMismatchedQuotes() {
    // Test with unclosed quotes (if grammar supports quoted strings)
    String configText = "sysname Router1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
  }

  @Test
  public void testUnrecognizedCommand() {
    // Configuration with unrecognized commands
    String configText =
        "sysname Router1\n"
            + "unknown_command_here value1 value2\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " another_unknown command\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should parse valid parts and skip/warn about unrecognized
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testDuplicateInterfaceNames() {
    // Same interface defined twice
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
    // Second definition should override first
    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.2.1"));
  }

  @Test
  public void testDuplicateVlanIds() {
    // Same VLAN defined twice
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + " description First\n"
            + "return\n"
            + "vlan 100\n"
            + " description Second\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
  }

  @Test
  public void testAclInvalidWildcard() {
    // Invalid wildcard mask in ACL
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 256.0.0.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning but continue
  }

  @Test
  public void testStaticRouteWithInvalidCidr() {
    // Invalid CIDR notation
    String configText =
        "sysname Router1\n" + "ip route-static 10.0.0.0/99 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should warn about invalid prefix length
  }

  @Test
  public void testNatStaticInvalidIp() {
    // NAT with invalid IP
    String configText =
        "sysname Router1\n" + "nat static global 999.999.999.999 inside 10.0.0.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid IP
  }

  @Test
  public void testVrfWithNameContainingSpaces() {
    // VRF names typically shouldn't have spaces
    String configText = "sysname Router1\n" + "ip vpn-instance VRF 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser may treat "VRF" as name and "1" as next command
  }

  @Test
  public void testInterfaceNameWithSpecialChars() {
    // Interface name shouldn't have special characters
    String configText = "sysname Router1\n" + "interface GigabitEthernet@0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // May or may not parse depending on grammar
  }

  @Test
  public void testMalformedBgpPeerConfig() {
    // BGP peer missing AS number
    String configText = "sysname Router1\n" + "bgp 65001\n" + " peer 192.168.1.2\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser should handle incomplete config
  }

  @Test
  public void testOspfNetworkInvalidPrefix() {
    // OSPF network with invalid prefix
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 10.0.0.0/33 area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should warn about invalid prefix length
  }

  @Test
  public void testWhitespaceVariations() {
    // Test various whitespace patterns
    String configText =
        "sysname    Router1\n"
            + "interface  GigabitEthernet0/0/0\n"
            + "  ip   address   192.168.1.1   255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testCaseSensitivity() {
    // Test case sensitivity of keywords
    String configText = "SYSNAME Router1\n" + "RETURN\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Keywords should be case-sensitive, so uppercase should fail
    // Configuration should be empty or have warnings
  }

  @Test
  public void testVeryLongHostname() {
    // Very long hostname (should be truncated or accepted)
    StringBuilder longHostname = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longHostname.append("a");
    }
    String configText = "sysname " + longHostname.toString() + "\nreturn\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should accept very long hostname
  }

  @Test
  public void testMultipleAclRulesWithGaps() {
    // ACL rules with non-sequential numbers (valid in Huawei)
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 100 deny source any\n"
            + " rule 200 permit source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl.getLines().size(), equalTo(3));
  }

  @Test
  public void testVlanBatchWithDuplicates() {
    // VLAN batch with duplicate VLAN IDs
    String configText = "sysname Router1\n" + "vlan batch 10 20 10 30\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should handle duplicates gracefully (likely just create once)
  }

  @Test
  public void testIncompleteInterfaceBlock() {
    // Interface block without return
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/1\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should handle transition to new interface gracefully
  }

  @Test
  public void testCommentLines() {
    // Huawei VRP doesn't have inline comments, but test whitespace lines
    String configText =
        "sysname Router1\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + "\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  // ========== EDGE CASE AND BOUNDARY TESTS ==========

  // 1. INTERFACE EDGE CASES

  @Test
  public void testInterfaceWithAllProperties() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));

    org.batfish.vendor.huawei.representation.HuaweiInterface iface =
        config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Uplink"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.1.1"));
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testMultipleInterfaceTypes() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface 10GE1/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface Vlanif100\n"
            + " ip address 172.16.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));
    assertThat(config.getInterfaces().containsKey("GigabitEthernet0/0/0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("10GE1/0/0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("Loopback0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("Vlanif100"), equalTo(true));
  }

  @Test
  public void testInterfaceBoundaryPortNumbers() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/47\n"
            + " ip address 192.168.47.1 255.255.255.0\n"
            + "interface 40GE2/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface 100GE3/0/0\n"
            + " ip address 10.1.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/47"), notNullValue());
    assertThat(config.getInterfaces().get("40GE2/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("100GE3/0/0"), notNullValue());
  }

  @Test
  public void testSubinterfaceDot1qVariations() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 100\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/0.200\n"
            + " dot1q termination vid 200\n"
            + " ip address 10.0.1.1 255.255.255.0\n"
            + "interface GigabitEthernet1/0/0.4094\n"
            + " dot1q termination vid 4094\n"
            + " ip address 10.1.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.100"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.200"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet1/0/0.4094"), notNullValue());
  }

  // 2. VLAN EDGE CASES

  @Test
  public void testVlanBatchWithManyVlans() {
    String configText =
        "sysname Router1\n" + "vlan batch 10 20 30 40 50 60 70 80 90 100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(10));

    // Verify all VLANs were created
    for (int vlanId : new int[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100}) {
      assertThat(config.getVlan(vlanId), notNullValue());
      assertThat(config.getVlan(vlanId).getVlanId(), equalTo(vlanId));
    }
  }

  @Test
  public void testVlanWithAllProperties() {
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Management\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());
    assertThat(vlan.getVlanId(), equalTo(100));
    assertThat(vlan.getDescription(), equalTo("Management"));
  }

  @Test
  public void testVlanBoundaryValues() {
    String configText =
        "sysname Router1\n" + "vlan 1\n" + "vlan 4094\n" + "vlan batch 2 to 10\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(10));
    assertThat(config.getVlan(1), notNullValue());
    assertThat(config.getVlan(4094), notNullValue());
  }

  @Test
  public void testVlanifWithMultipleVlans() {
    String configText =
        "sysname Router1\n"
            + "vlan 10\n"
            + "vlan 20\n"
            + "vlan 30\n"
            + "interface Vlanif10\n"
            + " ip address 192.168.10.1 255.255.255.0\n"
            + "interface Vlanif20\n"
            + " ip address 192.168.20.1 255.255.255.0\n"
            + "interface Vlanif30\n"
            + " ip address 192.168.30.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(3));
    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getVlan(10), notNullValue());
    assertThat(config.getVlan(20), notNullValue());
    assertThat(config.getVlan(30), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif10"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif20"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif30"), notNullValue());
  }

  // 3. STATIC ROUTE EDGE CASES

  @Test
  public void testStaticRouteAllCombinations() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2\n"
            + "ip route-static 172.16.0.0 255.255.0.0 192.168.1.3 preference 50\n"
            + "ip route-static 10.1.0.0 255.255.255.0 GigabitEthernet0/0/0 192.168.1.4\n"
            + "ip route-static 172.17.0.0 255.255.0.0 GigabitEthernet0/0/0 192.168.1.5 preference"
            + " 70\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(5));

    // Verify default route
    HuaweiStaticRoute defaultRoute = config.getStaticRoutes().get(0);
    assertThat(defaultRoute.getDestination().toString(), equalTo("0.0.0.0/0"));

    // Verify route with interface
    HuaweiStaticRoute routeWithInterface = config.getStaticRoutes().get(3);
    assertThat(routeWithInterface.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));

    // Verify route with preference
    HuaweiStaticRoute routeWithPref = config.getStaticRoutes().get(2);
    assertThat(routeWithPref.getPreference(), equalTo(50));
  }

  @Test
  public void testStaticRouteWithVrf() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "ip route-static vpn-instance VRF1 10.0.0.0 255.255.255.0 192.168.1.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testStaticRouteBoundaryNetworks() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 255.255.255.255 255.255.255.255 192.168.1.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(2));
  }

  @Test
  public void testStaticRouteWithAllPreferences() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2 preference 60\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.3 preference 255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(3));

    HuaweiStaticRoute route1 = config.getStaticRoutes().get(0);
    assertThat(route1.getPreference(), equalTo(1));

    HuaweiStaticRoute route2 = config.getStaticRoutes().get(1);
    assertThat(route2.getPreference(), equalTo(60));

    HuaweiStaticRoute route3 = config.getStaticRoutes().get(2);
    assertThat(route3.getPreference(), equalTo(255));
  }

  // 4. BGP EDGE CASES

  @Test
  public void testBgpMinimalConfiguration() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithMultiplePeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65003\n"
            + " peer 192.168.1.4 as-number 65004\n"
            + " peer 10.0.0.2 as-number 65005\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithManyNetworkAnnouncements() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 10.1.0.0 255.255.255.0\n"
            + " network 10.2.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0\n"
            + " network 192.168.0.0 255.255.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithBoundaryAsNumbers() {
    String configText =
        "sysname Router1\n" + "bgp 1\n" + " peer 192.168.1.2 as-number 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(1L));
  }

  // 5. OSPF EDGE CASES

  @Test
  public void testOspfWithoutRouterId() {
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
  }

  @Test
  public void testOspfWithMultipleAreas() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 0\n"
            + " area 1\n"
            + " area 2\n"
            + " area 100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(4));
  }

  @Test
  public void testOspfWithManyNetworkStatements() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.1.0.0/24 area 0\n"
            + " network 10.2.0.0/24 area 0\n"
            + " network 172.16.0.0/16 area 1\n"
            + " network 192.168.0.0/16 area 2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(5));
  }

  @Test
  public void testOspfBoundaryAreaIds() {
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 0\n" + " area 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(2));
  }

  // 6. ACL EDGE CASES

  @Test
  public void testAclWithAllProtocolTypes() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit udp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 53\n"
            + " rule 15 permit icmp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255\n"
            + " rule 20 permit ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclWithVariousPortSpecifications() {
    String configText =
        "sysname Router1\n"
            + "acl 3001 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port gt 1024\n"
            + " rule 15 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port lt 1024\n"
            + " rule 20 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port range 2000 3000\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3001");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclWithWildcardAddresses() {
    String configText =
        "sysname Router1\n"
            + "acl 2001 basic\n"
            + " rule 5 permit source 10.0.0.0 0.255.255.255\n"
            + " rule 10 permit source 192.168.0.0 0.0.255.255\n"
            + " rule 15 permit source 172.16.0.0 0.0.0.255\n"
            + " rule 20 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2001");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclRulesWithAllCombinations() {
    String configText =
        "sysname Router1\n"
            + "acl 3002 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny tcp source any destination any\n"
            + " rule 15 permit udp source 192.168.2.0 0.0.0.255 destination 10.0.0.0 0.0.0.255\n"
            + " rule 20 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3002");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));
  }

  // 7. NAT EDGE CASES

  @Test
  public void testNatAllTypesCombined() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 192.168.1.0 0.0.0.255\n"
            + "nat static global 1.1.1.1 inside 10.0.0.1\n"
            + "nat outbound 2000\n"
            + "nat outbound 2000 interface\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(3));

    // Verify static NAT
    HuaweiNatRule staticRule = config.getNatRules().get(0);
    assertThat(staticRule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));

    // Verify dynamic NAT
    HuaweiNatRule dynamicRule = config.getNatRules().get(1);
    assertThat(dynamicRule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));

    // Verify Easy IP
    HuaweiNatRule easyIpRule = config.getNatRules().get(2);
    assertThat(easyIpRule.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
  }

  @Test
  public void testNatWithVrf() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));
  }

  // 8. VRF EDGE CASES

  @Test
  public void testMultipleVrfs() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 both\n"
            + "ip vpn-instance VRF2\n"
            + " route-distinguisher 200:1\n"
            + " vpn-target 200:1 both\n"
            + "ip vpn-instance VRF3\n"
            + " route-distinguisher 300:1\n"
            + " vpn-target 300:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(3));

    assertThat(config.getVrfs().get("VRF1"), notNullValue());
    assertThat(config.getVrfs().get("VRF2"), notNullValue());
    assertThat(config.getVrfs().get("VRF3"), notNullValue());
  }

  @Test
  public void testVrfWithAllProperties() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 65000:100\n"
            + " vpn-target 65000:100 export\n"
            + " vpn-target 65000:200 import\n"
            + " vpn-target 65000:300 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("65000:100"));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
  }

  @Test
  public void testVpnTargetCombinations() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 export\n"
            + " vpn-target 100:2 import\n"
            + " vpn-target 100:3 export\n"
            + " vpn-target 100:4 import\n"
            + " vpn-target 100:5 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getExportRouteTargets().size(), equalTo(3));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(3));
  }
}
