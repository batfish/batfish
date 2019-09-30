package org.batfish.grammar.flatjuniper.parsing;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * FlatJuniper lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class FlatJuniperBaseLexer extends BatfishLexer {

  protected boolean enableIPV6_ADDRESS = true;
  protected boolean enableIP_ADDRESS = true;
  protected boolean enableDEC = true;
  protected boolean _markWildcards = false;

  private Integer _overrideTokenStartLine;
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  public FlatJuniperBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public Token emit() {
    Token t =
        _factory.create(
            _tokenFactorySourcePair,
            _type,
            _text,
            _channel,
            _tokenStartCharIndex,
            getCharIndex() - 1,
            firstNonNull(_overrideTokenStartLine, _tokenStartLine),
            _tokenStartCharPositionInLine);
    emit(t);
    return t;
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _secondToLastTokenType = _lastTokenType;
      _lastTokenType = token.getType();
    }
  }

  public boolean isPrefix() {
    char nextChar = (char) this.getInputStream().LA(1);
    if (Character.isDigit(nextChar) || nextChar == '.') {
      return false;
    }
    return true;
  }

  @Override
  public String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
    sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
    sb.append("enableDEC: " + enableDEC + "\n");
    sb.append("markWildcards: " + _markWildcards + "\n");
    return sb.toString();
  }

  public void setMarkWildcards(boolean markWildcards) {
    _markWildcards = markWildcards;
  }

  public void setOverrideTokenStartLine(Integer overrideTokenStartLine) {
    _overrideTokenStartLine = overrideTokenStartLine;
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }
}
