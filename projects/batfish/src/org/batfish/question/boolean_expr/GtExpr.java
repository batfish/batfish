package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.int_expr.IntExpr;

public class GtExpr extends BaseBooleanExpr {

   private final IntExpr _lhs;

   private final IntExpr _rhs;

   public GtExpr(IntExpr lhs, IntExpr rhs) {
      _lhs = lhs;
      _rhs = rhs;
   }

   @Override
   public Boolean evaluate(Environment env) {
      int lhs = _lhs.evaluate(env);
      int rhs = _rhs.evaluate(env);
      return lhs > rhs;
   }

}
