package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;

public class NeqExpr extends BaseBooleanExpr {

   private final Expr _lhs;

   private final Expr _rhs;

   public NeqExpr(Expr lhs, Expr rhs) {
      _lhs = lhs;
      _rhs = rhs;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Object lhs = _lhs.evaluate(environment);
      Object rhs = _rhs.evaluate(environment);
      return !lhs.equals(rhs);
   }

}
