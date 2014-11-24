package batfish.grammar.cisco;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.cisco.CiscoGrammar.Cisco_configurationContext;

public class CiscoCombinedParser extends
      BatfishCombinedParser<CiscoGrammar, CiscoGrammarCommonLexer> {

   public CiscoCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(CiscoGrammar.class, CiscoGrammarCommonLexer.class, input,
            throwOnParserError, throwOnParserError);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

}
