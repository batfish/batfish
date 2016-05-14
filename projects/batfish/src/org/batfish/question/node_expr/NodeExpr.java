package org.batfish.question.node_expr;

import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface NodeExpr extends Expr {

   @Override
   public Configuration evaluate(Environment environment);

}
