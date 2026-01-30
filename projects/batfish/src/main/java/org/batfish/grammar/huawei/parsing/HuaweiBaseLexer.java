package org.batfish.grammar.huawei.parsing;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Huawei hybrid lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class HuaweiBaseLexer extends BatfishLexer {

  public HuaweiBaseLexer(CharStream input) {
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

  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_lastTokenType: " + _lastTokenType + "\n");
    sb.append("_secondToLastTokenType: " + _secondToLastTokenType + "\n");
    return sb.toString();
  }
}
