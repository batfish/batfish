package org.batfish.question.map_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.QMap;

public interface MapExpr extends Expr {

   @Override
   QMap evaluate(Environment environment);

}
