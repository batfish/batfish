package batfish.grammar.topology;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;

public class RoleCombinedParser extends
      BatfishCombinedParser<RoleParser, RoleLexer> {

   public RoleCombinedParser(String input) {
      super(RoleParser.class, RoleLexer.class, input);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.role_declarations();
   }

}
