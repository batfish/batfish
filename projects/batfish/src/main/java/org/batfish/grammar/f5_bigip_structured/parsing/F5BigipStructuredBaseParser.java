package org.batfish.grammar.f5_bigip_structured.parsing;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * F5 BIG-IP structured parser base class providing validation functionality on top of {@link
 * BatfishParser}.
 */
@ParametersAreNonnullByDefault
public abstract class F5BigipStructuredBaseParser extends BatfishParser {

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

  /** Returns {@code true} iff {@code t}'s text represents a valid vlan ID (1-4094) in base 10. */
  protected static boolean isVlanId(Token t) {
    Integer val = Ints.tryParse(t.getText());
    return val != null && 1 <= val && val <= 4094;
  }

  public F5BigipStructuredBaseParser(TokenStream input) {
    super(input);
  }
}
