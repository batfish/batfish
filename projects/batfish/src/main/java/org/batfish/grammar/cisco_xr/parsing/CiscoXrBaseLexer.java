package org.batfish.grammar.cisco_xr.parsing;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** CiscoXr lexer base class providing additional functionality on top of {@link BatfishLexer}. */
@ParametersAreNonnullByDefault
public abstract class CiscoXrBaseLexer extends BatfishLexer {

  public CiscoXrBaseLexer(CharStream input) {
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

  protected final int accessGroupContextTokenType() {
    return _accessGroupContextTokenType;
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }

  // IOS banner utility functions
  protected final void setBannerIosDelimiter() {
    _bannerIosDelimiter = getText().codePointAt(0);
  }

  protected final boolean bannerIosDelimiterFollows() {
    return getInputStream().LA(1) == _bannerIosDelimiter;
  }

  protected final void unsetBannerIosDelimiter() {
    _bannerIosDelimiter = null;
  }

  /**
   * Set the type of the token controlling which mode {@code access-group} should push the lexer
   * into when it appears as the first word on the line.
   */
  protected final void setAccessGroupContextTokenType(int accessGroupContextTokenType) {
    _accessGroupContextTokenType = accessGroupContextTokenType;
  }

  private @Nullable Integer _bannerIosDelimiter;
  private int _accessGroupContextTokenType = -1;
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;
}
