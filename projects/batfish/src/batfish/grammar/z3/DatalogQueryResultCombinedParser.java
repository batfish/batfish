package batfish.grammar.z3;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class DatalogQueryResultCombinedParser extends
      BatfishCombinedParser<DatalogQueryResultParser, DatalogQueryResultLexer> {

   public DatalogQueryResultCombinedParser(String input) {
      super(DatalogQueryResultParser.class, DatalogQueryResultLexer.class,
            input);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.result();
   }
}
