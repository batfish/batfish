package org.batfish.question.prefix_space_expr;

import org.batfish.question.Environment;
import org.batfish.representation.PrefixSpace;

public final class VarPrefixSpaceExpr extends BasePrefixSpaceExpr {

   private final String _var;

   public VarPrefixSpaceExpr(String var) {
      _var = var;
   }

   @Override
   public PrefixSpace evaluate(Environment environment) {
      return environment.getPrefixSpaces().get(_var);
   }

}
