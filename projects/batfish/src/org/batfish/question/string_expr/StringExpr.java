package org.batfish.question.string_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface StringExpr extends Expr {

   @Override
   String evaluate(Environment environment);

}
