package org.batfish.grammar.recovery;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.recovery.RecoveryParser.RecoveryContext;

/**
 * A {@link BatfishCombinedParser} that uses recovery grammar but disables recovery. Used for
 * testing best-effort BatfishLexerErrorListener and BatfishParserErrorListener error handling.
 */
public final class NonRecoveryCombinedParser
    extends BatfishCombinedParser<RecoveryParser, RecoveryLexer> {

  public NonRecoveryCombinedParser(String input, GrammarSettings settings) {
    super(RecoveryParser.class, RecoveryLexer.class, input, settings);
  }

  @Override
  public RecoveryContext parse() {
    return _parser.recovery();
  }
}
