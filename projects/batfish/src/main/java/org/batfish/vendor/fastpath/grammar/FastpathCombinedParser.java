package org.batfish.vendor.fastpath.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Fastpath_configurationContext;

public class FastpathCombinedParser extends BatfishCombinedParser<FastpathParser, FastpathLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(FastpathLexer.NEWLINE, "\n");

  public FastpathCombinedParser(String input, GrammarSettings settings) {
    super(
        FastpathParser.class,
        FastpathLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Fastpath_configurationContext parse() {
    return _parser.fastpath_configuration();
  }
}
