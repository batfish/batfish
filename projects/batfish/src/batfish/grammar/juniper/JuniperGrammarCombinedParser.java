package batfish.grammar.juniper;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class JuniperGrammarCombinedParser extends
      BatfishCombinedParser<JuniperGrammarParser, JuniperGrammarLexer> {

   public JuniperGrammarCombinedParser(String input) {
      super(JuniperGrammarParser.class, JuniperGrammarLexer.class, input);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.juniper_configuration();
   }

}
