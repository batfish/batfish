package org.batfish.grammar.iptables;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.main.Settings;

public class IptablesCombinedParser
      extends BatfishCombinedParser<IptablesParser, IptablesLexer> {

   public IptablesCombinedParser(String input, Settings settings) {
      super(IptablesParser.class, IptablesLexer.class, input, settings);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.iptables_configuration();
   }

}
