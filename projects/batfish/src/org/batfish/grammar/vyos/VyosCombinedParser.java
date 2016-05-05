package org.batfish.grammar.vyos;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.main.Settings;

public class VyosCombinedParser extends
      BatfishCombinedParser<VyosParser, VyosLexer> {

   public VyosCombinedParser(String input, Settings settings) {
      super(VyosParser.class, VyosLexer.class, input, settings);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.vyos_configuration();
   }

}
