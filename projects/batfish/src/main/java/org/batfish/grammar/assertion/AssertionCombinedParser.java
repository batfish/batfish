package org.batfish.grammar.assertion;

import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.assertion.AssertionParser.AssertionContext;

public class AssertionCombinedParser
    extends BatfishCombinedParser<AssertionParser, AssertionLexer> {

  public AssertionCombinedParser(String input, Settings settings) {
    super(AssertionParser.class, AssertionLexer.class, input, settings);
  }

  @Override
  public AssertionContext parse() {
    return _parser.assertion();
  }
}
