package org.batfish.question.boolean_expr.prefix_space;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.prefix_space_expr.PrefixSpaceExpr;

public abstract class PrefixSpaceBooleanExpr extends BaseBooleanExpr {

   protected final PrefixSpaceExpr _caller;

   public PrefixSpaceBooleanExpr(PrefixSpaceExpr caller) {
      _caller = caller;
   }

}
