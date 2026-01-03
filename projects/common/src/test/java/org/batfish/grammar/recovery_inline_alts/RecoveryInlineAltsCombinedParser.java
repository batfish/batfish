package org.batfish.grammar.recovery_inline_alts;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.recovery_inline_alts.RecoveryInlineAltsParser.Recovery_inline_alts_configurationContext;

public class RecoveryInlineAltsCombinedParser
    extends BatfishCombinedParser<RecoveryInlineAltsParser, RecoveryInlineAltsLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
          RecoveryInlineAltsLexer.NEWLINE, "\n");

  public RecoveryInlineAltsCombinedParser(String input, GrammarSettings settings) {
    super(
        RecoveryInlineAltsParser.class,
        RecoveryInlineAltsLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  public RecoveryInlineAltsCombinedParser(
      String input, GrammarSettings settings, FlattenerLineMap lineMap) {
    super(
        RecoveryInlineAltsParser.class,
        RecoveryInlineAltsLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES,
        lineMap);
  }

  @Override
  public Recovery_inline_alts_configurationContext parse() {
    return _parser.recovery_inline_alts_configuration();
  }
}
