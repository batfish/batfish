package org.batfish.grammar.cumulus_frr.parsing;

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
}
