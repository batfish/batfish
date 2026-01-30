package org.batfish.grammar.huawei;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.representation.huawei.HuaweiConfiguration;
import org.batfish.representation.huawei.HuaweiConversions;
import org.batfish.representation.huawei.HuaweiStaticRoute;
import org.batfish.representation.huawei.HuaweiVlan;
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

    org.batfish.representation.huawei.HuaweiInterface iface =
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

    org.batfish.representation.huawei.HuaweiInterface iface =
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

    org.batfish.representation.huawei.HuaweiInterface iface =
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

    org.batfish.representation.huawei.HuaweiInterface iface =
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

    org.batfish.representation.huawei.HuaweiInterface iface =
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
}
