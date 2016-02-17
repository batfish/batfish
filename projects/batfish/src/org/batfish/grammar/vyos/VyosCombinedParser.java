package org.batfish.grammar.vyos;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class VyosCombinedParser extends
      BatfishCombinedParser<VyosParser, VyosLexer> {

   public VyosCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(VyosParser.class, VyosLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.vyos_configuration();
   }

}
