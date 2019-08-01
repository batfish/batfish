package org.batfish.grammar.cumulus_ports.parsing;

import org.antlr.v4.runtime.CharStream;
import org.batfish.grammar.BatfishLexer;

/**
 * Cumulus ports file lexer base class providing additional functionality on top of {@link
 * BatfishLexer}.
 */
public abstract class CumulusPortsBaseLexer extends BatfishLexer {
  public CumulusPortsBaseLexer(CharStream input) {
    super(input);
  }
}
