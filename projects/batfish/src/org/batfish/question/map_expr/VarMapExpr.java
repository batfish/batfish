package org.batfish.question.map_expr;

import org.batfish.question.Environment;
import org.batfish.question.QMap;

public final class VarMapExpr extends BaseMapExpr {

   private final String _var;

   public VarMapExpr(String var) {
      _var = var;
   }

   @Override
   public QMap evaluate(Environment environment) {
      return environment.getMaps().get(_var);
   }

}
