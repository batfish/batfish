package batfish.grammar.cisco;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.cisco.CiscoGrammar.Cisco_configurationContext;

public class CiscoCombinedParser extends
      BatfishCombinedParser<CiscoGrammar, CiscoGrammarCommonLexer> {

   public CiscoCombinedParser(String input) {
      super(CiscoGrammar.class, CiscoGrammarCommonLexer.class, input);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

}
