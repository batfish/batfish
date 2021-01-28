package org.batfish.grammar.juniper.parsing;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Juniper hierarchical lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
public abstract class JuniperBaseLexer extends BatfishLexer {

  public JuniperBaseLexer(CharStream input) {
    super(input);
    _lastTokenType = -1;
  }

  @Override
  public void emit(Token token) {
    super.emit(token);
    _lastTokenType = token.getType(); // note this includes HIDDEN channel
  }

  /**
   * The type of the last {@link Token} emitted on <i>any</i> channel, or {@code -1} if none has
   * been emitted yet.
   */
  protected int lastTokenType() {
    return _lastTokenType;
  }

  private int _lastTokenType;
}
