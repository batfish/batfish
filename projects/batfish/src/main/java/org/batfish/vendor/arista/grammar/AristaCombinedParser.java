package org.batfish.vendor.arista.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.arista.grammar.AristaParser.Arista_configurationContext;

public class AristaCombinedParser extends BatfishCombinedParser<AristaParser, AristaLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategyFactory(AristaLexer.NEWLINE, "\n");

  public AristaCombinedParser(String input, GrammarSettings settings) {
    super(
        AristaParser.class,
        AristaLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Arista_configurationContext parse() {
    return _parser.arista_configuration();
  }
}
