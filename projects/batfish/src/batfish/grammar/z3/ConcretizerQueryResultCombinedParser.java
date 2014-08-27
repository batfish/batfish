package batfish.grammar.z3;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class ConcretizerQueryResultCombinedParser
      extends
      BatfishCombinedParser<ConcretizerQueryResultParser, ConcretizerQueryResultLexer> {

   public ConcretizerQueryResultCombinedParser(String input) {
      super(ConcretizerQueryResultParser.class,
            ConcretizerQueryResultLexer.class, input);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.result();
   }
}
