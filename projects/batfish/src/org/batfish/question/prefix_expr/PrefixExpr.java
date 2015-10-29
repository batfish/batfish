package org.batfish.question.prefix_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Prefix;

public interface PrefixExpr extends Expr {

   @Override
   Prefix evaluate(Environment environment);

}
