package org.batfish.grammar.topology;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;

public class RoleCombinedParser
      extends BatfishCombinedParser<RoleParser, RoleLexer> {

   public RoleCombinedParser(String input, Settings settings) {
      super(RoleParser.class, RoleLexer.class, input, settings);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.role_declarations();
   }

}
