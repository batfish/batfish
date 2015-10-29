package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface BooleanExpr extends Expr {

   @Override
   public Boolean evaluate(Environment environment);

}
