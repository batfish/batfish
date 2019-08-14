package org.batfish.grammar.cumulus_frr;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Cumulus_frr_configurationContext;

public class CumulusFrrCombinedParser
    extends BatfishCombinedParser<CumulusFrrParser, CumulusFrrLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CumulusFrrLexer.NEWLINE, "\n");

  public CumulusFrrCombinedParser(String input, GrammarSettings settings, int line, int offset) {
    super(
        CumulusFrrParser.class,
        CumulusFrrLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
    _lexer.getInterpreter().setLine(line);
    _lexer.getInputStream().seek(offset);
  }

  @Override
  public Cumulus_frr_configurationContext parse() {
    return _parser.cumulus_frr_configuration();
  }
}
