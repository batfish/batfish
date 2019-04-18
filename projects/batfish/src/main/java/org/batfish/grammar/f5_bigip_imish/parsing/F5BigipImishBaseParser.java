package org.batfish.grammar.f5_bigip_imish.parsing;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * F5 BIG-IP imish parser base class providing validation functionality on top of {@link
 * BatfishParser}.
 */
@ParametersAreNonnullByDefault
public abstract class F5BigipImishBaseParser extends BatfishParser {

  /** Return {@code true} iff {@code t}'s text represents a valid IPv4 prefix-length in base 10. */
  protected static boolean isIpPrefixLength(Token t) {
    try {
      Integer val = Integer.parseInt(t.getText(), 10);
      checkArgument(0 <= val && val <= 32);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} iff {@code t}'s text represents a valid unsigned 32-bit integer in base
   * 10.
   */
  protected static boolean isUint32(Token t) {
    try {
      Long val = Long.parseLong(t.getText(), 10);
      checkArgument(val == (val & 0xFFFFFFFFL));
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  public F5BigipImishBaseParser(TokenStream input) {
    super(input);
  }
}
