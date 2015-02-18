package org.batfish.grammar.logicblox;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class LogiQLCombinedParser extends
      BatfishCombinedParser<LogiQLParser, LogiQLLexer> {

   public LogiQLCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(LogiQLParser.class, LogiQLLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.block();
   }

}
