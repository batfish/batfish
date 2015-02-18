package org.batfish.grammar.z3;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class DatalogQueryResultCombinedParser extends
      BatfishCombinedParser<DatalogQueryResultParser, DatalogQueryResultLexer> {

   public DatalogQueryResultCombinedParser(String input,
         boolean throwOnParserError, boolean throwOnLexerError) {
      super(DatalogQueryResultParser.class, DatalogQueryResultLexer.class,
            input, throwOnParserError, throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.result();
   }
}
