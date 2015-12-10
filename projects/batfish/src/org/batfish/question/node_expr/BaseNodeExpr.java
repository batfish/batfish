package org.batfish.question.node_expr;

import org.batfish.question.Environment;

public abstract class BaseNodeExpr implements NodeExpr {

   public static final String print(NodeExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
