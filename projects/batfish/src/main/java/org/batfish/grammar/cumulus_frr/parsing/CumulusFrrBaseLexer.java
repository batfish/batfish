package org.batfish.grammar.cumulus_frr.parsing;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cumulus frr.conf file lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
public abstract class CumulusFrrBaseLexer extends BatfishLexer {
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  public CumulusFrrBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _secondToLastTokenType = _lastTokenType;
      _lastTokenType = token.getType();
    }
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }
}
