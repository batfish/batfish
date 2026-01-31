package org.batfish.grammar.cisco_ftd.parsing;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** Cisco FTD lexer base class providing additional functionality on top of {@link BatfishLexer}. */
@ParametersAreNonnullByDefault
public abstract class FtdBaseLexer extends BatfishLexer {

  public FtdBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _lastTokenType = token.getType();
    }
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  /**
   * Checks if the upcoming text is a pure alphabetic sequence (no special characters). Used as a
   * semantic predicate to optimize NAME vs WORD disambiguation.
   *
   * <p>This helps the lexer fail-fast when NAME cannot possibly match, avoiding expensive
   * backtracking. Checks up to 10 characters ahead to determine if the sequence contains special
   * characters (which would indicate a NAME) or is purely alphabetic (which should be WORD).
   *
   * @return true if the upcoming text is purely alphabetic (a-z, A-Z), false otherwise
   */
  protected final boolean isPureAlphabeticSequence() {
    int la = 1;
    int charsToCheck = Math.min(10, _input.LA(la) != CharStream.EOF ? 10 : 0);

    for (int i = 0;
        i < charsToCheck
            && _input.LA(la) != CharStream.EOF
            && _input.LA(la) != '\n'
            && _input.LA(la) != '\r';
        i++) {
      int c = _input.LA(la);
      // Check if character is NOT a letter (a-z or A-Z)
      if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
        return false; // Found a non-alphabetic character (could be NAME)
      }
      la++;
    }
    return true; // All checked characters are alphabetic (should be WORD)
  }

  private int _lastTokenType = -1;

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_lastTokenType: " + _lastTokenType + "\n");
    return sb.toString();
  }
}
