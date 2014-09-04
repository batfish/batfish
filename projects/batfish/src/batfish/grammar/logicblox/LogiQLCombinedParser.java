package batfish.grammar.logicblox;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class LogiQLCombinedParser extends
      BatfishCombinedParser<LogiQLParser, LogiQLLexer> {

   public LogiQLCombinedParser(String input) {
      super(LogiQLParser.class, LogiQLLexer.class, input);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.block();
   }

}
