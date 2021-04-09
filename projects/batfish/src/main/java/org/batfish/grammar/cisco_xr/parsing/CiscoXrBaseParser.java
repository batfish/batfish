package org.batfish.grammar.cisco_xr.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/** CiscoXr parser base class providing validation functionality on top of {@link BatfishParser}. */
@ParametersAreNonnullByDefault
public abstract class CiscoXrBaseParser extends BatfishParser {

  public CiscoXrBaseParser(TokenStream input) {
    super(input);
  }
}
