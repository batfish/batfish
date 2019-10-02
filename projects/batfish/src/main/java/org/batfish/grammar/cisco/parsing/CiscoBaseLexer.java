package org.batfish.grammar.cisco.parsing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco hybrid lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class CiscoBaseLexer extends BatfishLexer {

  public CiscoBaseLexer(CharStream input) {
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

  public void setAsa(boolean asa) {
    _asa = asa;
  }

  public void setCadant(boolean cadant) {
    _cadant = cadant;
  }

  public void setEos(boolean eos) {
    _eos = eos;
  }

  public void setFoundry(boolean foundry) {
    _foundry = foundry;
  }

  public void setIos(boolean ios) {
    _ios = ios;
  }

  public void setIosXr(boolean iosXr) {
    _iosXr = iosXr;
  }

  protected final boolean isAsa() {
    return _asa;
  }

  protected final boolean isCadant() {
    return _cadant;
  }

  protected final boolean isEos() {
    return _eos;
  }

  protected final boolean isFoundry() {
    return _foundry;
  }

  protected final boolean isIos() {
    return _ios;
  }

  protected final boolean isIosXr() {
    return _iosXr;
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }

  // Cadant banner utility function
  protected final boolean bannerCadantDelimiterFollows() {
    if (!(getInputStream().LA(1) == '/'
        && getInputStream().LA(2) == 'e'
        && getInputStream().LA(3) == 'n'
        && getInputStream().LA(4) == 'd')) {
      return false;
    }
    int last = getInputStream().LA(5);
    return last == '\n' || last == '\r';
  }

  // EOS banner utility function
  protected final boolean bannerEosDelimiterFollows() {
    if (!(getInputStream().LA(1) == 'E'
        && getInputStream().LA(2) == 'O'
        && getInputStream().LA(3) == 'F')) {
      return false;
    }
    int last = getInputStream().LA(4);
    return last == '\n' || last == '\r';
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

  protected boolean _enableAclNum = false;
  protected boolean _enableDec = true;
  protected boolean _enableIpv6Address = true;
  protected boolean _enableIpAddress = true;
  protected boolean _enableCommunityListNum = false;
  protected boolean _enableRegex = false;

  protected boolean _inAccessList = false;
  protected boolean _inCommunitySet = false;

  private boolean _asa = false;
  private boolean _cadant = false;
  private boolean _eos = false;
  private boolean _foundry = false;
  private boolean _ios = false;
  private boolean _iosXr = false;

  private @Nullable Integer _bannerIosDelimiter;
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_asa: " + _asa + "\n");
    sb.append("_cadant: " + _cadant + "\n");
    sb.append("_eos: " + _eos + "\n");
    sb.append("_foundry: " + _foundry + "\n");
    sb.append("_ios: " + _ios + "\n");
    sb.append("_iosXr: " + _iosXr + "\n");
    sb.append("_enableAclNum: " + _enableAclNum + "\n");
    sb.append("_enableCommunityListNum: " + _enableCommunityListNum + "\n");
    sb.append("_enableDec: " + _enableDec + "\n");
    sb.append("_enableIpAddress: " + _enableIpAddress + "\n");
    sb.append("_enableIpv6Address: " + _enableIpv6Address + "\n");
    sb.append("_enableRegex: " + _enableRegex + "\n");
    sb.append("_inAccessList: " + _inAccessList + "\n");
    sb.append("_inCommunitySet: " + _inCommunitySet + "\n");
    return sb.toString();
  }
}
