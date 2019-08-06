package org.batfish.grammar.cumulus_interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
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
  public void testIface() {
    String input = "iface swp1\n";
    Interfaces interfaces = parse(input, new CumulusNcluConfiguration());
    assertThat(interfaces.getInterfaces().keySet(), contains("swp1"));
  }
}
