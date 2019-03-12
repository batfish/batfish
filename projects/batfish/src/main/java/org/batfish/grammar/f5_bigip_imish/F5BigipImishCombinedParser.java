package org.batfish.grammar.f5_bigip_imish;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.F5_bigip_imish_configurationContext;

public class F5BigipImishCombinedParser
    extends BatfishCombinedParser<F5BigipImishParser, F5BigipImishLexer> {

  public F5BigipImishCombinedParser(String input, GrammarSettings settings, int offset, int line) {
    super(F5BigipImishParser.class, F5BigipImishLexer.class, input, settings);
    _lexer.getInputStream().seek(offset);
    _lexer.getInterpreter().setLine(line);
  }

  @Override
  public F5_bigip_imish_configurationContext parse() {
    return _parser.f5_bigip_imish_configuration();
  }
}
