package org.batfish.grammar.topology;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class RoleCombinedParser extends
      BatfishCombinedParser<RoleParser, RoleLexer> {

   public RoleCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(RoleParser.class, RoleLexer.class, input, throwOnParserError,
            throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.role_declarations();
   }

}
