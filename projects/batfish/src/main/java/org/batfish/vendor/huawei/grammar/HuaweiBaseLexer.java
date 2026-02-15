package org.batfish.vendor.huawei.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** Huawei lexer base class providing additional functionality on top of {@link BatfishLexer}. */
public abstract class HuaweiBaseLexer extends BatfishLexer {

  public HuaweiBaseLexer(CharStream input) {
    super(input);
    _lastTokenType = -1;
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
