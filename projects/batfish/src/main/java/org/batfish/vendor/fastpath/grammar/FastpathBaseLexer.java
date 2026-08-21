package org.batfish.vendor.fastpath.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** FastPath lexer base class providing additional functionality on top of {@link BatfishLexer}. */
@ParametersAreNonnullByDefault
public abstract class FastpathBaseLexer extends BatfishLexer {

  private int _lastTokenType = -1;

  public FastpathBaseLexer(CharStream input) {
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
}
