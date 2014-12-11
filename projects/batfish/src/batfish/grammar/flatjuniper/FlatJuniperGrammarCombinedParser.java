package batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class FlatJuniperGrammarCombinedParser extends
      BatfishCombinedParser<FlatJuniperGrammarParser, FlatJuniperGrammarLexer> {

   public FlatJuniperGrammarCombinedParser(String input,
         boolean throwOnParserError, boolean throwOnLexerError) {
      super(FlatJuniperGrammarParser.class, FlatJuniperGrammarLexer.class,
            input, throwOnParserError, throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.flat_juniper_configuration();
   }

}
