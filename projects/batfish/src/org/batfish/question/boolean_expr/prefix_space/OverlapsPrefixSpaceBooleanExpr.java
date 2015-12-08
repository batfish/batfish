package org.batfish.question.boolean_expr.prefix_space;

import org.batfish.question.Environment;
import org.batfish.question.prefix_space_expr.PrefixSpaceExpr;
import org.batfish.representation.PrefixSpace;

public final class OverlapsPrefixSpaceBooleanExpr extends
      PrefixSpaceBooleanExpr {

   private final PrefixSpaceExpr _arg;

   public OverlapsPrefixSpaceBooleanExpr(PrefixSpaceExpr caller,
         PrefixSpaceExpr arg) {
      super(caller);
      _arg = arg;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      PrefixSpace caller = _caller.evaluate(environment);
      PrefixSpace arg = _arg.evaluate(environment);
      return caller.overlaps(arg);
   }

}
