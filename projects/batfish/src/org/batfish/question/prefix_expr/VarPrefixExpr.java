package org.batfish.question.prefix_expr;

import org.batfish.question.Environment;
import org.batfish.representation.Prefix;

public final class VarPrefixExpr extends BasePrefixExpr {

   private final String _var;

   public VarPrefixExpr(String var) {
      _var = var;
   }

   @Override
   public Prefix evaluate(Environment environment) {
      return environment.getPrefixes().get(_var);
   }

}
