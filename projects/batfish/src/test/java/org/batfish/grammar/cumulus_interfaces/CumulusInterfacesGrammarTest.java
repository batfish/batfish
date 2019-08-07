package org.batfish.grammar.cumulus_interfaces;

import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus_interfaces.Interface;
import org.batfish.representation.cumulus_interfaces.Interfaces;
import org.junit.Test;

/** Test of {@link CumulusInterfacesParser}. */
public class CumulusInterfacesGrammarTest {

  private static Interfaces parse(String input, CumulusNcluConfiguration config) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);
    CumulusInterfacesCombinedParser parser =
        new CumulusInterfacesCombinedParser(input, settings, 1, 0);
    Cumulus_interfaces_configurationContext ctxt = parser.parse();
    Warnings w = new Warnings();
    CumulusInterfacesConfigurationBuilder configurationBuilder =
        new CumulusInterfacesConfigurationBuilder(config, w);
    new BatfishParseTreeWalker(parser).walk(configurationBuilder, ctxt);
    return configurationBuilder.getInterfaces();
  }

  @Test
  public void testAuto() {
    String input = "auto swp1\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    assertThat(interfaces.getAutoIfaces(), contains("swp1"));
  }

  @Test
  public void testBondSlaves() {
    String input = "iface i1\n bond-slaves i2 i3 i4\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertThat(iface.getBondSlaves(), contains("i2", "i3", "i4"));
  }

  @Test
  public void testIface() {
    String input = "iface swp1\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    assertThat(interfaces.getInterfaces(), hasKeys("swp1"));
  }

  @Test
  public void testIfaceAddress() {
    String input = "iface i1\n address 10.12.13.14/24\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertThat(iface.getAddresses(), contains(ConcreteInterfaceAddress.parse("10.12.13.14/24")));
  }

  @Test
  public void testIfaceBridgePorts() {
    String input = "iface i1\n bridge-ports i2 i3 i4\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertThat(iface.getBridgePorts(), contains("i2", "i3", "i4"));
  }

  @Test
  public void testIfaceBridgeVids() {
    String input = "iface i1\n bridge-vids 1 2 3 4\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertThat(iface.getBridgeVids().enumerate(), contains(1, 2, 3, 4));
  }

  @Test
  public void testIfaceLinkSpeed() {
    String input = "iface i1\n link-speed 10000\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertEquals(iface.getLinkSpeed(), (Integer) 10000);
  }

  @Test
  public void testIfaceLinkSpeed_null() {
    String input = "iface i1\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertNull(iface.getLinkSpeed());
  }

  @Test
  public void testIfaceVrf() {
    String input = "iface i1\n vrf v1\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("i1");
    assertThat(iface.getVrf(), equalTo("v1"));
  }

  @Test
  public void testIfaceVrfTable() {
    String input = "iface vrf1\n vrf-table auto\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    Interface iface = interfaces.getInterfaces().get("vrf1");
    assertTrue(iface.getIsVrf());
  }
}
