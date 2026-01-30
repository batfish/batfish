package org.batfish.grammar.huawei;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.representation.huawei.HuaweiConfiguration;
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
}
