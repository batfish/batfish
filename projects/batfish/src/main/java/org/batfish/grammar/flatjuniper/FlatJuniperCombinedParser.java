package org.batfish.grammar.flatjuniper;

import javax.annotation.Nullable;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flattener.FlattenerLineMap;

public class FlatJuniperCombinedParser
    extends BatfishCombinedParser<FlatJuniperParser, FlatJuniperLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
          FlatJuniperLexer.NEWLINE, "\n");

  FlatJuniperCombinedParser(String input, GrammarSettings settings) {
    this(input, settings, null);
  }

  public FlatJuniperCombinedParser(
      String input, GrammarSettings settings, @Nullable FlattenerLineMap lineMap) {
    super(
        FlatJuniperParser.class,
        FlatJuniperLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES,
        lineMap);
  }

  @Override
  public Flat_juniper_configurationContext parse() {
    return _parser.flat_juniper_configuration();
  }

  public void setMarkWildcards(boolean markWildcards) {
    _lexer.setMarkWildcards(markWildcards);
  }
}
