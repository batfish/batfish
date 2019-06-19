package org.batfish.grammar.mrv;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;

public class MrvCombinedParser extends BatfishCombinedParser<MrvParser, MrvLexer> {

  public MrvCombinedParser(String input, GrammarSettings settings) {
    super(MrvParser.class, MrvLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.mrv_configuration();
  }
}
