package org.batfish.grammar.juniper;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;

public class JuniperCombinedParser extends BatfishCombinedParser<JuniperParser, JuniperLexer> {

  public JuniperCombinedParser(String input, Settings settings) {
    super(JuniperParser.class, JuniperLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.juniper_configuration();
  }
}
