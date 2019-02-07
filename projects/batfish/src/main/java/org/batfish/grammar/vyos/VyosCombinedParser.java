package org.batfish.grammar.vyos;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;

public class VyosCombinedParser extends BatfishCombinedParser<VyosParser, VyosLexer> {

  public VyosCombinedParser(String input, GrammarSettings settings) {
    super(VyosParser.class, VyosLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.vyos_configuration();
  }
}
