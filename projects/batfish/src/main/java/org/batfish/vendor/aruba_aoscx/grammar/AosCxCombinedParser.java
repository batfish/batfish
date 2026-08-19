package org.batfish.vendor.aruba_aoscx.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.Aoscx_configurationContext;

public class AosCxCombinedParser extends BatfishCombinedParser<AosCxParser, AosCxLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategyFactory(AosCxLexer.NEWLINE, "\n");

  public AosCxCombinedParser(String input, GrammarSettings settings) {
    super(
        AosCxParser.class,
        AosCxLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Aoscx_configurationContext parse() {
    return _parser.aoscx_configuration();
  }
}
