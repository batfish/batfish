package org.batfish.grammar.palo_alto_nested;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;

public class PaloAltoNestedCombinedParser
    extends BatfishCombinedParser<PaloAltoNestedParser, PaloAltoNestedLexer> {

  public PaloAltoNestedCombinedParser(String input, GrammarSettings settings) {
    super(PaloAltoNestedParser.class, PaloAltoNestedLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.palo_alto_nested_configuration();
  }
}
