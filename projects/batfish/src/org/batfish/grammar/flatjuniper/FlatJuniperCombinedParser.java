package org.batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;

public class FlatJuniperCombinedParser
      extends BatfishCombinedParser<FlatJuniperParser, FlatJuniperLexer> {

   public FlatJuniperCombinedParser(String input, Settings settings) {
      super(FlatJuniperParser.class, FlatJuniperLexer.class, input, settings);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.flat_juniper_configuration();
   }

}
