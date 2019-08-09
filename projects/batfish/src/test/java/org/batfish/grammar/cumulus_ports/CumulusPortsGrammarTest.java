package org.batfish.grammar.cumulus_ports;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Cumulus_ports_configurationContext;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Interface;
import org.junit.Before;
import org.junit.Test;

public final class CumulusPortsGrammarTest {
  CumulusNcluConfiguration _config;

  @Before
  public void setup() {
    _config = new CumulusNcluConfiguration();
  }

  private Interface addInterface(String ifaceName, CumulusInterfaceType type) {
    Interface iface = new Interface(ifaceName, type, null, null);
    _config.getInterfaces().put(ifaceName, iface);
    return iface;
  }

  private void parse(String input) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);
    CumulusPortsCombinedParser parser = new CumulusPortsCombinedParser(input, settings, 1, 0);
    Cumulus_ports_configurationContext ctxt = parser.parse();
    Warnings w = new Warnings();
    CumulusPortsConfigurationBuilder configurationBuilder =
        new CumulusPortsConfigurationBuilder(_config, parser, w);
    new BatfishParseTreeWalker(parser).walk(configurationBuilder, ctxt);
  }

  @Test
  public void testDisabled() {
    Interface iface = addInterface("swp1", CumulusInterfaceType.PHYSICAL);
    assertFalse(iface.isDisabled());
    parse("1=disabled\n");
    assertTrue(iface.isDisabled());
  }

  @Test
  public void testSpeed() {
    Interface iface = addInterface("swp1", CumulusInterfaceType.PHYSICAL);
    iface.setSpeed(null);
    parse("1=100G\n");
    assertThat(iface.getSpeed(), equalTo(100000)); // speed is in Mbps
  }
}
