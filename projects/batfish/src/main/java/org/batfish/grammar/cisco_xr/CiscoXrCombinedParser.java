package org.batfish.grammar.cisco_xr;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cisco_xr.CiscoXrParser.Cisco_xr_configurationContext;

public class CiscoXrCombinedParser extends BatfishCombinedParser<CiscoXrParser, CiscoXrLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CiscoXrLexer.NEWLINE, "\n");

  public CiscoXrCombinedParser(String input, GrammarSettings settings) {
    super(
        CiscoXrParser.class,
        CiscoXrLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Cisco_xr_configurationContext parse() {
    return _parser.cisco_xr_configuration();
  }
}
