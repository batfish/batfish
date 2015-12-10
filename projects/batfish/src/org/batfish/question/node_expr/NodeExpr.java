package org.batfish.question.node_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Configuration;

public interface NodeExpr extends Expr {

   @Override
   public Configuration evaluate(Environment environment);

}
