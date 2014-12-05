package batfish.grammar.flatjuniper;

import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;

public class GroupPruner extends FlatJuniperGrammarParserBaseListener {

   public void exitS_groups(S_groupsContext ctx) {
      ctx.getParent().children.remove(ctx);
   }

}
