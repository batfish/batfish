package org.batfish.grammar.recovery_rule_alts;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.recovery_rule_alts.RecoveryRuleAltsParser.Recovery_rule_alts_configurationContext;

public class RecoveryRuleAltsCombinedParser
    extends BatfishCombinedParser<RecoveryRuleAltsParser, RecoveryRuleAltsLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
          RecoveryRuleAltsLexer.NEWLINE, "\n");

  public RecoveryRuleAltsCombinedParser(String input, GrammarSettings settings) {
    super(
        RecoveryRuleAltsParser.class,
        RecoveryRuleAltsLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  public RecoveryRuleAltsCombinedParser(
      String input, GrammarSettings settings, FlattenerLineMap lineMap) {
    super(
        RecoveryRuleAltsParser.class,
        RecoveryRuleAltsLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES,
        lineMap);
  }

  @Override
  public Recovery_rule_alts_configurationContext parse() {
    return _parser.recovery_rule_alts_configuration();
  }
}
