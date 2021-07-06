package org.batfish.vendor.check_point_gateway.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Check Point gateway lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class CheckPointGatewayBaseLexer extends BatfishLexer {

  public CheckPointGatewayBaseLexer(CharStream input) {
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
