package org.batfish.vendor.rtx.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.rtx.grammar.RtxParser.Rtx_configurationContext;

/** Combined lexer and parser for Yamaha RTX configurations. */
public class RtxCombinedParser extends BatfishCombinedParser<RtxParser, RtxLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(RtxLexer.NEWLINE, "\n");

  public RtxCombinedParser(String input, GrammarSettings settings) {
    super(
        RtxParser.class,
        RtxLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Rtx_configurationContext parse() {
    return _parser.rtx_configuration();
  }
}
