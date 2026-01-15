package org.batfish.grammar.cisco_ftd.parsing;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco FTD lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class FtdBaseLexer extends BatfishLexer {

  public FtdBaseLexer(CharStream input) {
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

  private int _lastTokenType = -1;

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_lastTokenType: " + _lastTokenType + "\n");
    return sb.toString();
  }
}
