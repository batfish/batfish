package org.batfish.question.prefix_space_expr;

import org.batfish.datamodel.PrefixSpace;
import org.batfish.question.Environment;

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
