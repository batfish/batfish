package org.batfish.grammar.fortios.parsing;

import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/** FortiOS parser base class providing additional functionality on top of {@link BatfishParser}. */
public abstract class FortiosBaseParser extends BatfishParser {

  public FortiosBaseParser(TokenStream input) {
    super(input);
  }
}
