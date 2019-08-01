package org.batfish.grammar.cumulus_ports.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Cumulus_ports_configurationContext;
import org.junit.Test;

/** Unit tests for {@link CumulusPortsCombinedParser}. */
public class CumulusPortsCombinedParserTest {
  @Test
  public void detectStartOfInterfacesFile() {
    String input = "# This file describes the network interfaces";
    CumulusPortsCombinedParser parser =
        new CumulusPortsCombinedParser(input, MockGrammarSettings.builder().build());
    Cumulus_ports_configurationContext ctxt = parser.parse();
    assertThat(ctxt.getStart().getLine(), equalTo(1));
    assertThat(ctxt.getStart().getStartIndex(), equalTo(0));
  }
}
