package org.batfish.vendor.sonic.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.sonic.SonicLexer;
import org.batfish.grammar.sonic.SonicParser;

/** Combined parser for sonic files. Essentially a noop parser. */
public class SonicCombinedParser extends BatfishCombinedParser<SonicParser, SonicLexer> {
  public SonicCombinedParser(String input, GrammarSettings settings) {
    super(SonicParser.class, SonicLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.noop();
  }
}
