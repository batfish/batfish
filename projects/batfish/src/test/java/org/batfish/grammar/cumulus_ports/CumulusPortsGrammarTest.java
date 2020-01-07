package org.batfish.grammar.cumulus_ports;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Cumulus_ports_configurationContext;
import org.batfish.representation.cumulus.CumulusConcatenatedConfiguration;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.junit.Before;
import org.junit.Test;

public final class CumulusPortsGrammarTest {
  CumulusConcatenatedConfiguration _config;
  Warnings _warnings;

  @Before
  public void setup() {
    _config = new CumulusConcatenatedConfiguration();
    _warnings = new Warnings();
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
    _config = SerializationUtils.clone(_config);
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
    parse("1=4x25G\n");

    PortSettings settings = new PortSettings();
    settings.setSpeed(25000);

    assertThat(
        _config.getPortsConfiguration().getPortSettings(),
        equalTo(
            ImmutableMap.of(
                "swp1s0", settings, "swp1s1", settings, "swp1s2", settings, "swp1s3", settings)));
  }

  @Test
  public void testDisabled() {
    parse("1=disabled\n");
    PortSettings settings = new PortSettings();
    settings.setDisabled(true);
    assertThat(
        _config.getPortsConfiguration().getPortSettings(),
        equalTo(ImmutableMap.of("swp1", settings)));
  }

  @Test
  public void testSpeed() {
    parse("1=100G\n");
    PortSettings settings = new PortSettings();
    settings.setSpeed(100000);
    assertThat(
        _config.getPortsConfiguration().getPortSettings(),
        equalTo(ImmutableMap.of("swp1", settings)));
  }
}
