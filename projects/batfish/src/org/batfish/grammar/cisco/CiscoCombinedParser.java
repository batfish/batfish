package org.batfish.grammar.cisco;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.cisco.CiscoParser.Cisco_configurationContext;
import org.batfish.main.Settings;

public class CiscoCombinedParser
      extends BatfishCombinedParser<CiscoParser, CiscoLexer> {

   public CiscoCombinedParser(String input, Settings settings,
         boolean multilineBgpNeighbors) {
      super(CiscoParser.class, CiscoLexer.class, input, settings);
      _parser.setMultilineBgpNeighbors(multilineBgpNeighbors);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

}
