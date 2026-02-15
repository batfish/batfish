package org.batfish.vendor.huawei.grammar;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.huawei.representation.HuaweiAclRule.Action;
import org.batfish.vendor.huawei.representation.HuaweiBgpPeer;
import org.batfish.vendor.huawei.representation.HuaweiBgpProcess;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiOspfArea;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiVrf;
import org.junit.Test;

/** Tests for {@link HuaweiControlPlaneExtractor}. */
public class HuaweiGrammarTest {

  private static HuaweiConfiguration parseVendorConfig(String config) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    HuaweiCombinedParser parser = new HuaweiCombinedParser(config, settings);
    Warnings parseWarnings = new Warnings();
    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(config, parser, parseWarnings);
    ParserRuleContext tree =
        org.batfish.main.Batfish.parse(
            parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    return (HuaweiConfiguration) extractor.getVendorConfiguration();
  }

  @Test
  public void testMinimalConfig() {
    String config =
        "# Minimal config\n"
            + "sysname Router1\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("router1"));
    assertThat(huaweiConfig.getInterfaces().size(), equalTo(1));
    assertThat(
        huaweiConfig.getInterfaces().get("GigabitEthernet0/0/0").getDescription(),
        equalTo("Uplink"));
    assertThat(
        huaweiConfig.getInterfaces().get("GigabitEthernet0/0/0").getAddress().toString(),
        equalTo("192.168.1.1/24"));
  }

  @Test
  public void testBgpConfig() {
    String config =
        "sysname BgpRouter\n"
            + "\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("bgprouter"));
    assertThat(huaweiConfig.getBgpProcess().getAsNum(), equalTo(65001L));
    assertThat(huaweiConfig.getBgpProcess().getRouterId().toString(), equalTo("1.1.1.1"));
    assertThat(huaweiConfig.getBgpProcess().getPeers().size(), equalTo(1));
  }

  @Test
  public void testOspfConfig() {
    String config =
        "sysname OspfRouter\n"
            + "\n"
            + "ospf 1\n"
            + " router-id 2.2.2.2\n"
            + " area 0\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("ospfrouter"));
    assertThat(huaweiConfig.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(huaweiConfig.getOspfProcess().getRouterId().toString(), equalTo("2.2.2.2"));
    assertThat(huaweiConfig.getOspfProcess().getAreas().size(), equalTo(1));
    assertThat(huaweiConfig.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
  }

  @Test
  public void testVlanConfig() {
    String config = "sysname VlanRouter\n" + "\n" + "vlan 10\n" + "vlan 20\n" + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("vlanrouter"));
    assertThat(huaweiConfig.getVlans().size(), equalTo(2));
    assertThat(huaweiConfig.getVlans().containsKey(10), equalTo(true));
    assertThat(huaweiConfig.getVlans().containsKey(20), equalTo(true));
  }

  @Test
  public void testAclConfig() {
    String config =
        "sysname AclRouter\n"
            + "\n"
            + "acl 2000\n"
            + " rule 5 permit\n"
            + " rule 10 deny\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("aclrouter"));
    assertThat(huaweiConfig.getAcls().size(), equalTo(1));
    assertThat(huaweiConfig.getAcls().get("2000"), notNullValue());
    assertThat(huaweiConfig.getAcls().get("2000").getRules(), hasSize(2));
    assertThat(huaweiConfig.getAcls().get("2000").getRules().get(0).getNumber(), equalTo(5));
    assertThat(
        huaweiConfig.getAcls().get("2000").getRules().get(0).getAction(), equalTo(Action.PERMIT));
    assertThat(huaweiConfig.getAcls().get("2000").getRules().get(1).getNumber(), equalTo(10));
    assertThat(
        huaweiConfig.getAcls().get("2000").getRules().get(1).getAction(), equalTo(Action.DENY));
  }

  @Test
  public void testVrfConfig() {
    String config =
        "sysname VrfRouter\n"
            + "\n"
            + "ip vpn-instance VRF_A\n"
            + " router-id 65000:100\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getHostname(), equalTo("vrfrouter"));
    assertThat(huaweiConfig.getVrfs().size(), equalTo(1));
    assertThat(huaweiConfig.getVrfs().get("VRF_A"), notNullValue());
    assertThat(huaweiConfig.getVrfs().get("VRF_A").getRouteDistinguisher(), equalTo("65000:100"));
  }

  @Test
  public void testInterfaceShutdown() {
    String config =
        "sysname ShutdownRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/1\n"
            + " description Active interface\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface GigabitEthernet0/0/2\n"
            + " description Shutdown interface\n"
            + " ip address 10.0.1.1 255.255.255.0\n"
            + " shutdown\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getInterfaces().size(), equalTo(2));

    // Active interface should not be shutdown
    assertThat(
        huaweiConfig.getInterfaces().get("GigabitEthernet0/0/1").getShutdown(), equalTo(false));

    // Shutdown interface should be shutdown
    assertThat(
        huaweiConfig.getInterfaces().get("GigabitEthernet0/0/2").getShutdown(), equalTo(true));
  }

  @Test
  public void testMultipleInterfaces() {
    String config =
        "sysname MultiIfRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description First interface\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface GigabitEthernet0/0/1\n"
            + " description Second interface\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface LoopBack0\n"
            + " description Loopback\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    assertThat(huaweiConfig.getInterfaces().size(), equalTo(3));
    assertThat(huaweiConfig.getInterfaces().containsKey("GigabitEthernet0/0/0"), equalTo(true));
    assertThat(huaweiConfig.getInterfaces().containsKey("GigabitEthernet0/0/1"), equalTo(true));
    assertThat(huaweiConfig.getInterfaces().containsKey("LoopBack0"), equalTo(true));
  }

  @Test
  public void testVIConversion() throws IOException {
    String config =
        "sysname ViTestRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Test interface\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " peer 192.168.0.2 as-number 65002\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    java.util.List<Configuration> configs = huaweiConfig.toVendorIndependentConfigurations();

    assertThat(configs, hasSize(1));
    Configuration c = configs.get(0);

    // Check hostname
    assertThat(c.getHostname(), equalTo("vitestrouter"));

    // Check human name (raw hostname)
    assertThat(c.getHumanName(), equalTo("ViTestRouter"));

    // Check interface conversion
    assertThat(c, hasInterface("GigabitEthernet0/0/0", hasDescription(equalTo("Test interface"))));
    assertThat(c, hasInterface("GigabitEthernet0/0/0", hasAddress("192.168.0.1/24")));

    // Check BGP conversion
    assertThat(c.getDefaultVrf().getBgpProcess(), notNullValue());
    assertThat(c.getDefaultVrf().getBgpProcess().getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(c.getDefaultVrf().getBgpProcess().getActiveNeighbors().size(), equalTo(1));
  }

  @Test
  public void testVIConversionInterfaceShutdown() throws IOException {
    String config =
        "sysname ShutdownViTest\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 10.0.1.1 255.255.255.0\n"
            + " shutdown\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    java.util.List<Configuration> configs = huaweiConfig.toVendorIndependentConfigurations();
    Configuration c = configs.get(0);

    // Active interface should be active
    assertThat(c, hasInterface("GigabitEthernet0/0/0", isActive()));

    // Shutdown interface should not be active
    assertThat(c, hasInterface("GigabitEthernet0/0/1", isActive(false)));
  }

  @Test
  public void testBgpRouterIdInference() {
    // Test that BGP router ID is inferred from Loopback0
    String config =
        "sysname BgpInferRouter\n"
            + "\n"
            + "interface LoopBack0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "return\n"
            + "\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    java.util.List<Configuration> configs = huaweiConfig.toVendorIndependentConfigurations();
    Configuration c = configs.get(0);

    // Router ID should be inferred from Loopback0
    assertThat(c.getDefaultVrf().getBgpProcess().getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testBgpWithoutAsNumber() {
    String config =
        "sysname BgpNoAsRouter\n" + "\n" + "bgp 65001\n" + " peer 192.168.1.2\n" + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    HuaweiBgpProcess bgp = huaweiConfig.getBgpProcess();

    assertThat(bgp, notNullValue());
    assertThat(bgp.getPeers().size(), equalTo(1));

    HuaweiBgpPeer peer = bgp.getPeers().get(Ip.parse("192.168.1.2"));
    assertThat(peer, notNullValue());
    assertThat(peer.getAsNum(), nullValue());
  }

  @Test
  public void testInterfaceTypes() {
    String config =
        "sysname IfTypeRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface LoopBack0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "return\n"
            + "\n"
            + "interface Vlanif100\n"
            + " ip address 192.168.100.1 255.255.255.0\n"
            + "return\n"
            + "\n"
            + "interface Eth-Trunk1\n"
            + " ip address 172.16.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    java.util.List<Configuration> configs = huaweiConfig.toVendorIndependentConfigurations();
    Configuration c = configs.get(0);

    // Check interface types
    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/0/0").getInterfaceType(),
        equalTo(InterfaceType.PHYSICAL));
    assertThat(
        c.getAllInterfaces().get("LoopBack0").getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(
        c.getAllInterfaces().get("Vlanif100").getInterfaceType(), equalTo(InterfaceType.VLAN));
    assertThat(
        c.getAllInterfaces().get("Eth-Trunk1").getInterfaceType(),
        equalTo(InterfaceType.AGGREGATED));
  }

  @Test
  public void testOspfWithArea() {
    String config =
        "sysname OspfAreaRouter\n"
            + "\n"
            + "ospf 1\n"
            + " router-id 1.1.1.1\n"
            + " area 0\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    HuaweiOspfProcess ospf = huaweiConfig.getOspfProcess();

    assertThat(ospf, notNullValue());
    assertThat(ospf.getProcessId(), equalTo(1L));
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(ospf.getAreas().containsKey(0L), equalTo(true));
  }

  @Test
  public void testEmptyConfig() {
    String config = "# Empty config\n\nreturn\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);

    assertThat(huaweiConfig.getHostname(), nullValue());
    assertThat(huaweiConfig.getInterfaces(), anEmptyMap());
    assertThat(huaweiConfig.getVlans(), anEmptyMap());
    assertThat(huaweiConfig.getAcls(), anEmptyMap());
    assertThat(huaweiConfig.getVrfs(), anEmptyMap());
    assertThat(huaweiConfig.getBgpProcess(), nullValue());
    assertThat(huaweiConfig.getOspfProcess(), nullValue());
  }

  @Test
  public void testInterfaceUndoShutdown() {
    String config =
        "sysname UndoShutdownRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " shutdown\n"
            + " undo shutdown\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    HuaweiInterface iface = huaweiConfig.getInterfaces().get("GigabitEthernet0/0/0");

    // undo shutdown should set shutdown to false
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testAclWithName() {
    String config =
        "sysname AclNameRouter\n"
            + "\n"
            + "acl ACL_WEB\n"
            + " rule 5 permit\n"
            + " rule 10 deny\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);

    assertThat(huaweiConfig.getAcls().size(), equalTo(1));
    assertThat(huaweiConfig.getAcls().get("ACL_WEB"), notNullValue());
    assertThat(huaweiConfig.getAcls().get("ACL_WEB").getName(), equalTo("ACL_WEB"));
  }

  @Test
  public void testVrfName() {
    String config =
        "sysname VrfNameRouter\n"
            + "\n"
            + "ip vpn-instance CUSTOMER_A\n"
            + " router-id 65000:100\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);

    assertThat(huaweiConfig.getVrfs().size(), equalTo(1));
    HuaweiVrf vrf = huaweiConfig.getVrfs().get("CUSTOMER_A");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("CUSTOMER_A"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("65000:100"));
  }

  @Test
  public void testMultipleBgpPeers() {
    String config =
        "sysname MultiPeerRouter\n"
            + "\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65003\n"
            + " peer 192.168.1.4 as-number 65004\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    HuaweiBgpProcess bgp = huaweiConfig.getBgpProcess();

    assertThat(bgp.getPeers().size(), equalTo(3));
    assertThat(bgp.getPeers().containsKey(Ip.parse("192.168.1.2")), equalTo(true));
    assertThat(bgp.getPeers().containsKey(Ip.parse("192.168.1.3")), equalTo(true));
    assertThat(bgp.getPeers().containsKey(Ip.parse("192.168.1.4")), equalTo(true));
  }

  @Test
  public void testInterfaceDescription() {
    String config =
        "sysname DescRouter\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description This is a long description with multiple words\n"
            + "return\n";

    HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
    HuaweiInterface iface = huaweiConfig.getInterfaces().get("GigabitEthernet0/0/0");

    assertThat(iface.getDescription(), equalTo("This is a long description with multiple words"));
  }

  @Test
  public void testHuaweiConfigurationSetters() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    config.setHostname("TestRouter");
    assertThat(config.getHostname(), equalTo("testrouter"));
    assertThat(config.getRawHostname(), equalTo("TestRouter"));

    HuaweiBgpProcess bgp = new HuaweiBgpProcess(65001);
    config.setBgpProcess(bgp);
    assertThat(config.getBgpProcess(), equalTo(bgp));

    HuaweiOspfProcess ospf = new HuaweiOspfProcess(1);
    config.setOspfProcess(ospf);
    assertThat(config.getOspfProcess(), equalTo(ospf));
  }

  @Test
  public void testHuaweiBgpProcessSetters() {
    HuaweiBgpProcess bgp = new HuaweiBgpProcess(65001);

    bgp.setAsNum(65002);
    assertThat(bgp.getAsNum(), equalTo(65002L));

    bgp.setRouterId(Ip.parse("1.1.1.1"));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testHuaweiOspfProcessSetters() {
    HuaweiOspfProcess ospf = new HuaweiOspfProcess(1);

    ospf.setProcessId(2);
    assertThat(ospf.getProcessId(), equalTo(2L));

    ospf.setRouterId(Ip.parse("2.2.2.2"));
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testHuaweiInterfaceSetters() {
    HuaweiInterface iface = new HuaweiInterface("TestInterface");

    iface.setDescription("Test description");
    assertThat(iface.getDescription(), equalTo("Test description"));

    iface.setShutdown(true);
    assertThat(iface.getShutdown(), equalTo(true));

    assertThat(iface.getName(), equalTo("TestInterface"));
  }

  @Test
  public void testHuaweiAclRule() {
    org.batfish.vendor.huawei.representation.HuaweiAclRule rule =
        new org.batfish.vendor.huawei.representation.HuaweiAclRule(5, Action.PERMIT);

    assertThat(rule.getNumber(), equalTo(5));
    assertThat(rule.getAction(), equalTo(Action.PERMIT));
  }

  @Test
  public void testHuaweiOspfArea() {
    HuaweiOspfArea area = new HuaweiOspfArea(0);

    assertThat(area.getAreaId(), equalTo(0L));
  }

  @Test
  public void testHuaweiVlan() {
    org.batfish.vendor.huawei.representation.HuaweiVlan vlan =
        new org.batfish.vendor.huawei.representation.HuaweiVlan(100);

    assertThat(vlan.getId(), equalTo(100));
  }
}
