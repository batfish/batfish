package org.batfish.grammar.cisco_asa.parsing;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco hybrid lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class AsaBaseLexer extends BatfishLexer {

  public AsaBaseLexer(CharStream input) {
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

  protected boolean _enableAclNum = false;
  protected boolean _enableDec = true;
  protected boolean _enableIpv6Address = true;
  protected boolean _enableIpAddress = true;
  protected boolean _enableCommunityListNum = false;
  protected boolean _enableRegex = false;

  protected boolean _inAccessList = false;

  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_enableAclNum: " + _enableAclNum + "\n");
    sb.append("_enableCommunityListNum: " + _enableCommunityListNum + "\n");
    sb.append("_enableDec: " + _enableDec + "\n");
    sb.append("_enableIpAddress: " + _enableIpAddress + "\n");
    sb.append("_enableIpv6Address: " + _enableIpv6Address + "\n");
    sb.append("_enableRegex: " + _enableRegex + "\n");
    sb.append("_inAccessList: " + _inAccessList + "\n");
    return sb.toString();
  }
}
