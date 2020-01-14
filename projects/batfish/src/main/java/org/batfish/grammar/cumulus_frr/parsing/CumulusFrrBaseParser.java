package org.batfish.grammar.cumulus_frr.parsing;

import static com.google.common.base.Preconditions.checkArgument;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cumulus frr.conf file parser base class providing additional functionality on top of {@link
 * BatfishParser}.
 */
public abstract class CumulusFrrBaseParser extends BatfishParser {
  public CumulusFrrBaseParser(TokenStream input) {
    super(input);
  }

  /**
   * Returns {@code true} iff {@code t}'s text represents a valid vlan VNI number (1-16777215) in
   * base 10.
   */
  protected static boolean isVniNumber(ParserRuleContext v) {
    try {
      int val = Integer.parseInt(v.getText(), 10);
      checkArgument(1 <= val && val <= 0xFFFFFF);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
