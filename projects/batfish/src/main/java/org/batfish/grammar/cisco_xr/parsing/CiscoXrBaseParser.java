package org.batfish.grammar.cisco_xr.parsing;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/** CiscoXr parser base class providing validation functionality on top of {@link BatfishParser}. */
@ParametersAreNonnullByDefault
public abstract class CiscoXrBaseParser extends BatfishParser {

  /** Returns {@code true} iff {@code t}'s text represents a valid vlan ID (1-4094) in base 10. */
  protected static boolean isVlanId(Token t) {
    try {
      int val = Integer.parseInt(t.getText());
      checkArgument(1 <= val && val <= 4094);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  public CiscoXrBaseParser(TokenStream input) {
    super(input);
  }
}
