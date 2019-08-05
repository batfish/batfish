package org.batfish.grammar.cumulus_concatenated;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;

/** Combined parser for concatenated cumulus files. Essentially a noop parser. */
public class CumulusConcatenatedCombinedParser
    extends BatfishCombinedParser<CumulusConcatenatedParser, CumulusConcatenatedLexer> {
  public CumulusConcatenatedCombinedParser(String input, GrammarSettings settings) {
    super(CumulusConcatenatedParser.class, CumulusConcatenatedLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.noop();
  }
}
