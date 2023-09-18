package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerNoViableAltException;

public abstract class BatfishLexer extends Lexer {

  public static final int UNMATCHABLE_TOKEN = Integer.MAX_VALUE;

  public static final int UNRECOGNIZED_LINE_TOKEN = Integer.MAX_VALUE - 1;

  private BatfishCombinedParser<?, ?> _parser;

  private @Nullable BatfishLexerRecoveryStrategy _recoveryStrategy;

  public BatfishLexer(CharStream input) {
    super(input);
  }

  public String getMode() {
    return getModeNames()[_mode];
  }

  public @Nullable BatfishLexerRecoveryStrategy getRecoveryStrategy() {
    return _recoveryStrategy;
  }

  /** Return a string consisting of the next {@code size} characters in the stream. */
  public @Nonnull String lookAheadString(int size) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= size; i++) {
      sb.append((char) _input.LA(i));
    }
    return sb.toString();
  }

  /**
   * Return a string consisting of the next {@code size} characters in the stream after whitespace
   * is skipped.
   */
  public @Nonnull String lookAheadStringSkipWhitespace(int size) {
    StringBuilder sb = new StringBuilder();
    int start = 1;
    while (isWhitespace(_input.LA(start))) {
      start++;
    }
    for (int i = start; i < start + size; i++) {
      sb.append((char) _input.LA(i));
    }
    return sb.toString();
  }

  public boolean isWhitespace(int c) {
    return c == ' ' || c == '\t';
  }

  public void initErrorListener(BatfishCombinedParser<?, ?> parser) {
    _parser = parser;
    BatfishLexerErrorListener errorListener =
        new BatfishLexerErrorListener(getClass().getSimpleName(), parser);
    removeErrorListeners();
    addErrorListener(errorListener);
    _parser.setLexerErrorListener(errorListener);
  }

  @Override
  public void mode(int m) {
    _parser.updateTokenModes(_mode);
    super.mode(m);
  }

  /**
   * Print custom lexer state (should be overridden)
   *
   * @return Should print custom lexer state variables and their values
   */
  public String printStateVariables() {
    return "";
  }

  @Override
  public void recover(LexerNoViableAltException e) {
    if (_recoveryStrategy != null) {
      _recoveryStrategy.recover();
    } else {
      super.recover(e);
    }
  }

  public void setRecoveryStrategy(@Nullable BatfishLexerRecoveryStrategy recoveryStrategy) {
    _recoveryStrategy = recoveryStrategy;
  }

  /** Unconsume the text for current token and emit nothing. */
  protected final void less() {
    _input.seek(_tokenStartCharIndex);
    _type = SKIP;
  }
}
