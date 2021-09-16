package org.batfish.vendor.a10.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** A10 lexer base class providing additional functionality on top of {@link BatfishLexer}. */
public abstract class A10BaseLexer extends BatfishLexer {

  public A10BaseLexer(CharStream input) {
    super(input);
    _lastTokenType = -1;
  }

  public boolean followedByNewline() {
    int followedBy = _input.LA(1);
    return followedBy == '\n' || followedBy == '\r';
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _lastTokenType = token.getType();
    }
  }

  /**
   * The type of the last {@link Token} emitted on the default channel, or {@code -1} if none has
   * been emitted yet.
   */
  protected int lastTokenType() {
    return _lastTokenType;
  }

  private int _lastTokenType;
}
