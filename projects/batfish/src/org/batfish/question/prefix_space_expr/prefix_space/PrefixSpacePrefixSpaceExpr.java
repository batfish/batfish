package org.batfish.question.prefix_space_expr.prefix_space;

import org.batfish.question.prefix_space_expr.BasePrefixSpaceExpr;
import org.batfish.question.prefix_space_expr.PrefixSpaceExpr;

public abstract class PrefixSpacePrefixSpaceExpr extends BasePrefixSpaceExpr {

   protected final PrefixSpaceExpr _caller;

   public PrefixSpacePrefixSpaceExpr(PrefixSpaceExpr caller) {
      _caller = caller;
   }

}
