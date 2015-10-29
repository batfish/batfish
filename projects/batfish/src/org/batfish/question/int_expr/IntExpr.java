package org.batfish.question.int_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface IntExpr extends Expr {

   @Override
   Integer evaluate(Environment environment);

}
