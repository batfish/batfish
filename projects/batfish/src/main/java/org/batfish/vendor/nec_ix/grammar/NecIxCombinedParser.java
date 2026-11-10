package org.batfish.vendor.nec_ix.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Nec_ix_configurationContext;

/** Combined lexer and parser for NEC IX configurations. */
public class NecIxCombinedParser extends BatfishCombinedParser<NecIxParser, NecIxLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(NecIxLexer.NEWLINE, "\n");

  public NecIxCombinedParser(String input, GrammarSettings settings) {
    super(
        NecIxParser.class,
        NecIxLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Nec_ix_configurationContext parse() {
    return _parser.nec_ix_configuration();
  }
}
