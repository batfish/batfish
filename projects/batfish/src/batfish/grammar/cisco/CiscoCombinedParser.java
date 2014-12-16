package batfish.grammar.cisco;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.cisco.CiscoGrammarParser.Cisco_configurationContext;

public class CiscoCombinedParser extends
      BatfishCombinedParser<CiscoGrammarParser, CiscoGrammarLexer> {

   public CiscoCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(CiscoGrammarParser.class, CiscoGrammarLexer.class, input,
            throwOnParserError, throwOnParserError);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

}
