package org.batfish.question.prefix_expr;

import org.batfish.common.datamodel.Prefix;
import org.batfish.question.Environment;

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
