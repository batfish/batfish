package org.batfish.vendor.cisco_ftd.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cisco FTD parser base class providing validation functionality on top of {@link BatfishParser}.
 */
@ParametersAreNonnullByDefault
public abstract class FtdBaseParser extends BatfishParser {

  public FtdBaseParser(TokenStream input) {
    super(input);
  }
}
