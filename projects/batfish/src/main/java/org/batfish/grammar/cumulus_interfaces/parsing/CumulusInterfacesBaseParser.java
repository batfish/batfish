package org.batfish.grammar.cumulus_interfaces.parsing;

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
}
