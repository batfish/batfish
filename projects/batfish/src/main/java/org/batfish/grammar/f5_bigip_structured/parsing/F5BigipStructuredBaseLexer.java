package org.batfish.grammar.f5_bigip_structured.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * F5 BIG-IP structured lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class F5BigipStructuredBaseLexer extends BatfishLexer {

  public F5BigipStructuredBaseLexer(CharStream input) {
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

  // Helper methods for managing transition between regular grammar and rule TCL grammar

  protected final boolean isLtmRuleDeclaration() {
    return _ltmRuleDeclaration;
  }

  protected final void setLtmRuleDeclaration() {
    _ltmRuleDeclaration = true;
  }

  protected final void unsetLtmRuleDeclaration() {
    _ltmRuleDeclaration = false;
  }

  private int _lastTokenType = -1;
  private boolean _ltmRuleDeclaration;
  private int _secondToLastTokenType = -1;
}
