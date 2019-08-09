package org.batfish.grammar.cumulus_interfaces.parsing;

import static com.google.common.base.Preconditions.checkArgument;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cumulus interfaces file parser base class providing additional functionality on top of {@link
 * BatfishParser}.
 */
public abstract class CumulusInterfacesBaseParser extends BatfishParser {
  public CumulusInterfacesBaseParser(TokenStream input) {
    super(input);
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
}
