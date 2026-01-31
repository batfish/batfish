package org.batfish.grammar.cisco_ftd;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cisco_ftd.FtdParser.Ftd_configurationContext;

public class FtdCombinedParser extends BatfishCombinedParser<FtdParser, FtdLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(FtdLexer.NEWLINE, "\n");

  public FtdCombinedParser(String input, GrammarSettings settings) {
    super(
        FtdParser.class,
        FtdLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Ftd_configurationContext parse() {
    return _parser.ftd_configuration();
  }
}
