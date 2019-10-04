package org.batfish.grammar.cumulus_ports;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Cumulus_ports_configurationContext;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Interface;
import org.junit.Before;
import org.junit.Test;

public final class CumulusPortsGrammarTest {
  CumulusNcluConfiguration _config;
  Warnings _warnings;

  @Before
  public void setup() {
    _config = new CumulusNcluConfiguration();
    _warnings = new Warnings();
  }

  private Interface addInterface(String ifaceName, CumulusInterfaceType type) {
    Interface iface = new Interface(ifaceName, type, null, null);
    _config.getInterfaces().put(ifaceName, iface);
    return iface;
  }

  private void parse(String input) {
    GrammarSettings settings =
        MockGrammarSettings.builder()
            .setDisableUnrecognized(true)
            .setThrowOnLexerError(true)
            .setThrowOnParserError(true)
            .build();
    CumulusPortsCombinedParser parser = new CumulusPortsCombinedParser(input, settings, 1, 0);
    Cumulus_ports_configurationContext ctxt = parser.parse();
    CumulusPortsConfigurationBuilder configurationBuilder =
        new CumulusPortsConfigurationBuilder(_config, parser, _warnings);
    new BatfishParseTreeWalker(parser).walk(configurationBuilder, ctxt);
  }

  @Test
  public void testComment() {
    parse("# a \n # b");
  }

  @Test
  public void testBlankLine() {
    parse(" \n \n\n");
  }

  @Test
  public void testBreakout() {
    Interface s0 = addInterface("swp1s0", CumulusInterfaceType.PHYSICAL_SUBINTERFACE);
    Interface s1 = addInterface("swp1s1", CumulusInterfaceType.PHYSICAL_SUBINTERFACE);
    Interface s2 = addInterface("swp1s2", CumulusInterfaceType.PHYSICAL_SUBINTERFACE);
    Interface s3 = addInterface("swp1s3", CumulusInterfaceType.PHYSICAL_SUBINTERFACE);

    s0.setSpeed(null);
    s1.setSpeed(null);
    s2.setSpeed(null);
    s3.setSpeed(null);

    parse("1=4x25G\n");

    int speedMbps = 25000;
    assertThat(s0.getSpeed(), equalTo(speedMbps));
    assertThat(s1.getSpeed(), equalTo(speedMbps));
    assertThat(s2.getSpeed(), equalTo(speedMbps));
    assertThat(s3.getSpeed(), equalTo(speedMbps));
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

  @Test
  public void testMissingInterface() {
    parse("1=100G\n");
    assertThat(_warnings.getParseWarnings(), not(empty()));
  }
}
