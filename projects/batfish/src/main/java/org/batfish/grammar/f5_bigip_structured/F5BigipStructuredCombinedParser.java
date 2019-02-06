package org.batfish.grammar.f5_bigip_structured;

import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.F5_bigip_structured_configurationContext;

public class F5BigipStructuredCombinedParser
    extends BatfishCombinedParser<F5BigipStructuredParser, F5BigipStructuredLexer> {

  public F5BigipStructuredCombinedParser(String input, Settings settings) {
    super(F5BigipStructuredParser.class, F5BigipStructuredLexer.class, input, settings);
  }

  @Override
  public F5_bigip_structured_configurationContext parse() {
    return _parser.f5_bigip_structured_configuration();
  }
}
