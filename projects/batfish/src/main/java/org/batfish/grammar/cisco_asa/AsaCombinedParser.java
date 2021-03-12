package org.batfish.grammar.cisco_asa;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cisco_asa.AsaParser.Asa_configurationContext;

public class AsaCombinedParser extends BatfishCombinedParser<AsaParser, AsaLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(AsaLexer.NEWLINE, "\n");

  public AsaCombinedParser(String input, GrammarSettings settings) {
    super(
        AsaParser.class,
        AsaLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Asa_configurationContext parse() {
    return _parser.asa_configuration();
  }
}
