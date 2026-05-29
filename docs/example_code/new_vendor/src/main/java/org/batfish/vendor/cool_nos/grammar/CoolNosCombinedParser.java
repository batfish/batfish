package org.batfish.vendor.cool_nos.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.cool_nos.grammar.CoolNosParser.Cool_nos_configurationContext;

public class CoolNosCombinedParser extends BatfishCombinedParser<CoolNosParser, CoolNosLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CoolNosLexer.NEWLINE, "\n");

  public CoolNosCombinedParser(String input, GrammarSettings settings) {
    super(
        CoolNosParser.class,
        CoolNosLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Cool_nos_configurationContext parse() {
    return _parser.cool_nos_configuration();
  }
}
