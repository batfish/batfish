package org.batfish.grammar.cisco_nxos.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco NX-OS lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class CiscoNxosBaseLexer extends BatfishLexer {

  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  public CiscoNxosBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _secondToLastTokenType = _lastTokenType;
      _lastTokenType = token.getType();
    }
  }

  protected int lastTokenType() {
    return _lastTokenType;
  }

  protected int secondToLastTokenType() {
    return _secondToLastTokenType;
  }
}
