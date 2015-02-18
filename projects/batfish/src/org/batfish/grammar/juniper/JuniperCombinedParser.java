package org.batfish.grammar.juniper;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class JuniperCombinedParser extends
      BatfishCombinedParser<JuniperParser, JuniperLexer> {

   public JuniperCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(JuniperParser.class, JuniperLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.juniper_configuration();
   }

}
