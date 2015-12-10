package org.batfish.question.prefix_space_expr.prefix_space;

import org.batfish.question.Environment;
import org.batfish.question.prefix_space_expr.PrefixSpaceExpr;
import org.batfish.representation.PrefixSpace;

public class IntersectionPrefixSpacePrefixSpaceExpr extends
      PrefixSpacePrefixSpaceExpr {

   private final PrefixSpaceExpr _arg;

   public IntersectionPrefixSpacePrefixSpaceExpr(PrefixSpaceExpr caller,
         PrefixSpaceExpr arg) {
      super(caller);
      _arg = arg;
   }

   @Override
   public PrefixSpace evaluate(Environment environment) {
      PrefixSpace caller = _caller.evaluate(environment);
      PrefixSpace arg = _arg.evaluate(environment);
      return caller.intersection(arg);
   }

}
