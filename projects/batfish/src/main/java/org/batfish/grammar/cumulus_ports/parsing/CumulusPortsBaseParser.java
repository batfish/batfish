package org.batfish.grammar.cumulus_ports.parsing;

import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cumulus ports file parser base class providing additional functionality on top of {@link
 * BatfishParser}.
 */
public abstract class CumulusPortsBaseParser extends BatfishParser {
  public CumulusPortsBaseParser(TokenStream input) {
    super(input);
  }
}
