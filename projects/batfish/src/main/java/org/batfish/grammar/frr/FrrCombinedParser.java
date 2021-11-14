package org.batfish.grammar.frr;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.frr.FrrParser.Frr_configurationContext;

public class FrrCombinedParser extends BatfishCombinedParser<FrrParser, FrrLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(FrrLexer.NEWLINE, "\n");

  public FrrCombinedParser(String input, GrammarSettings settings, int line, int offset) {
    super(
        FrrParser.class,
        FrrLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
    _lexer.getInterpreter().setLine(line);
    _lexer.getInputStream().seek(offset);
  }

  @Override
  public Frr_configurationContext parse() {
    return _parser.frr_configuration();
  }
}
