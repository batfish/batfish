package org.batfish.grammar.cumulus_nclu.parsing;

import static com.google.common.base.Preconditions.checkArgument;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cumulus NCLU parser base class providing validation functionality on top of {@link
 * BatfishParser}.
 */
public abstract class CumulusNcluBaseParser extends BatfishParser {
  /**
   * Returns {@code true} iff {@code t}'s text represents a valid unsigned 16-bit integer in base
   * 10.
   */
  protected static boolean isUint16(Token t) {
    try {
      int val = Integer.parseInt(t.getText(), 10);
      checkArgument(val == (val & 0xFFFF));
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
      long val = Long.parseLong(t.getText(), 10);
      checkArgument(val == (val & 0xFFFFFFFFL));
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /** Returns {@code true} iff {@code t}'s text represents a valid vlan ID (1-4094) in base 10. */
  protected static boolean isVlanId(Token t) {
    try {
      int val = Integer.parseInt(t.getText(), 10);
      checkArgument(1 <= val && val <= 4094);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} iff {@code t}'s text represents a valid vlan VNI number (1-16777215) in
   * base 10.
   */
  protected static boolean isVniNumber(Token t) {
    try {
      int val = Integer.parseInt(t.getText(), 10);
      checkArgument(1 <= val && val <= 0xFFFFFF);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /** Returns {@code true} iff {@code t}'s text is "0". */
  protected static boolean isZero(Token t) {
    return t.getText().equals("0");
  }

  public CumulusNcluBaseParser(TokenStream input) {
    super(input);
  }
}
