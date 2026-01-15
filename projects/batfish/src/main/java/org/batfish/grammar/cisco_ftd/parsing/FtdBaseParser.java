package org.batfish.grammar.cisco_ftd.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/** Cisco FTD parser base class providing validation functionality on top of {@link BatfishParser}. */
@ParametersAreNonnullByDefault
public abstract class FtdBaseParser extends BatfishParser {

  public FtdBaseParser(TokenStream input) {
    super(input);
  }
}
