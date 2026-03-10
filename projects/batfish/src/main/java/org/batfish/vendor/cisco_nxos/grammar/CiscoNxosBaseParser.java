package org.batfish.vendor.cisco_nxos.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/**
 * Cisco NX-OS parser base class providing additional functionality on top of {@link BatfishParser}.
 */
@ParametersAreNonnullByDefault
public abstract class CiscoNxosBaseParser extends BatfishParser {

  public CiscoNxosBaseParser(TokenStream input) {
    super(input);
  }
}
