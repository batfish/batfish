package org.batfish.grammar.palo_alto_nested.parsing;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Palo alto hierarchical lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
public abstract class PaloAltoNestedBaseLexer extends BatfishLexer {

  public PaloAltoNestedBaseLexer(CharStream input) {
    super(input);
    _lastTokenType = -1;
  }

  @Override
  public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN || token.getText().matches("[\\r\\n]+")) {
      _lastTokenType = token.getType();
    }
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
