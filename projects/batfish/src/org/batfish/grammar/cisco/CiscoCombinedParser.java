package org.batfish.grammar.cisco;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.cisco.CiscoParser.Cisco_configurationContext;

public class CiscoCombinedParser extends
      BatfishCombinedParser<CiscoParser, CiscoLexer> {

   public CiscoCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(CiscoParser.class, CiscoLexer.class, input, throwOnParserError,
            throwOnParserError);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

}
