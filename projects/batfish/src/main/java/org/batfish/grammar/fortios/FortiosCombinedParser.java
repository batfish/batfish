package org.batfish.grammar.fortios;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.fortios.FortiosParser.Fortios_configurationContext;

public class FortiosCombinedParser extends BatfishCombinedParser<FortiosParser, FortiosLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(FortiosLexer.NEWLINE, "\n");

  public FortiosCombinedParser(String input, GrammarSettings settings) {
    super(
        FortiosParser.class,
        FortiosLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Fortios_configurationContext parse() {
    return _parser.fortios_configuration();
  }
}
