package org.batfish.vendor.cisco_nxos.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.cisco_nxos.grammar.CiscoNxosParser.Cisco_nxos_configurationContext;

public class CiscoNxosCombinedParser
    extends BatfishCombinedParser<CiscoNxosParser, CiscoNxosLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CiscoNxosLexer.NEWLINE, "\n");

  public CiscoNxosCombinedParser(String input, GrammarSettings settings) {
    super(
        CiscoNxosParser.class,
        CiscoNxosLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Cisco_nxos_configurationContext parse() {
    return _parser.cisco_nxos_configuration();
  }
}
