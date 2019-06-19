package org.batfish.grammar.palo_alto;

import javax.annotation.Nullable;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;

public class PaloAltoCombinedParser extends BatfishCombinedParser<PaloAltoParser, PaloAltoLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(PaloAltoLexer.NEWLINE, "\n");

  public PaloAltoCombinedParser(
      String input, GrammarSettings settings, @Nullable FlattenerLineMap lineMap) {
    super(
        PaloAltoParser.class,
        PaloAltoLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES,
        lineMap);
  }

  @Override
  public Palo_alto_configurationContext parse() {
    return _parser.palo_alto_configuration();
  }
}
