package org.batfish.grammar.cumulus_interfaces.parsing;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cumulus interfaces file lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
public abstract class CumulusInterfacesBaseLexer extends BatfishLexer {
  private int _lastTokenType = -1;

  public CumulusInterfacesBaseLexer(CharStream input) {
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
