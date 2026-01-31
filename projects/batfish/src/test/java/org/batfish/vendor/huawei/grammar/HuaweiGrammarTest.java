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
}
