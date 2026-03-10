package org.batfish.vendor.cisco_nxos.grammar;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco NX-OS lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class CiscoNxosBaseLexer extends BatfishLexer {

  private @Nullable Integer _bannerDelimiter;
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  public CiscoNxosBaseLexer(CharStream input) {
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

  protected final void setBannerDelimiter() {
    _bannerDelimiter = getText().codePointAt(0);
  }

  protected final boolean bannerDelimiterFollows() {
    return getInputStream().LA(1) == _bannerDelimiter;
  }

  protected final void unsetBannerDelimiter() {
    _bannerDelimiter = null;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }
}
