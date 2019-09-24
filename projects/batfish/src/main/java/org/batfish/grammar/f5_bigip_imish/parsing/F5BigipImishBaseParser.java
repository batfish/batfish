package org.batfish.grammar.f5_bigip_imish.parsing;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
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
    Integer val = Ints.tryParse(t.getText());
    return val != null && 0 <= val && val <= 32;
  }

  /**
   * Returns {@code true} iff {@code t}'s text represents a valid unsigned 16-bit integer in base
   * 10.
   */
  protected static boolean isUint16(Token t) {
    Integer val = Ints.tryParse(t.getText());
    return val != null && val == (val & 0xFFFFL);
  }

  /**
   * Returns {@code true} iff {@code t}'s text represents a valid unsigned 32-bit integer in base
   * 10.
   */
  protected static boolean isUint32(Token t) {
    Long val = Longs.tryParse(t.getText());
    return val != null && val == (val & 0xFFFFFFFFL);
  }

  public F5BigipImishBaseParser(TokenStream input) {
    super(input);
  }
}
