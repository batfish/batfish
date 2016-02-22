package org.batfish.grammar.mrv;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class MrvCombinedParser extends
      BatfishCombinedParser<MrvParser, MrvLexer> {

   public MrvCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(MrvParser.class, MrvLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.mrv_configuration();
   }

}
