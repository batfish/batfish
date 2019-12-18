package org.batfish.grammar.palo_alto.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco NX-OS lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class PaloAltoBaseLexer extends BatfishLexer {

  public PaloAltoBaseLexer(CharStream input) {
    super(input);
  }

  public boolean followedByNewline() {
    char followedBy = (char) _input.LA(1);
    return followedBy == '\n' || followedBy == '\r';
  }
}
