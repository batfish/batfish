package org.batfish.grammar.z3;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class ConcretizerQueryResultCombinedParser
      extends
      BatfishCombinedParser<ConcretizerQueryResultParser, ConcretizerQueryResultLexer> {

   public ConcretizerQueryResultCombinedParser(String input,
         boolean throwOnParserError, boolean throwOnLexerError) {
      super(ConcretizerQueryResultParser.class,
            ConcretizerQueryResultLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.result();
   }
}
