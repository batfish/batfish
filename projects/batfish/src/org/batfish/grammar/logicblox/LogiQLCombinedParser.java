package org.batfish.grammar.logicblox;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.main.Settings;

public class LogiQLCombinedParser
      extends BatfishCombinedParser<LogiQLParser, LogiQLLexer> {

   public LogiQLCombinedParser(String input, Settings settings) {
      super(LogiQLParser.class, LogiQLLexer.class, input, settings);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.block();
   }

}
