package org.batfish.question.prefix_expr;

import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface PrefixExpr extends Expr {

   @Override
   Prefix evaluate(Environment environment);

}
