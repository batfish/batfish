package org.batfish.vendor.sros.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.sros.grammar.SrosParser.Sros_configurationContext;

/** Combined lexer + parser for Nokia SR-OS (MD-CLI) configurations. */
public class SrosCombinedParser extends BatfishCombinedParser<SrosParser, SrosLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(SrosLexer.NEWLINE, "\n");

  public SrosCombinedParser(String input, GrammarSettings settings) {
    super(
        SrosParser.class,
        SrosLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Sros_configurationContext parse() {
    return _parser.sros_configuration();
  }
}
