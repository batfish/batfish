package org.batfish.vendor.a10.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.a10.grammar.A10Parser.A10_configurationContext;

public class A10CombinedParser extends BatfishCombinedParser<A10Parser, A10Lexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(A10Lexer.NEWLINE, "\n");

  public A10CombinedParser(String input, GrammarSettings settings) {
    super(
        A10Parser.class,
        A10Lexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public A10_configurationContext parse() {
    return _parser.a10_configuration();
  }
}
