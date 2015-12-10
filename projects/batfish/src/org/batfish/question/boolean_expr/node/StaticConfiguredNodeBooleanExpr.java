package org.batfish.question.boolean_expr.node;

import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.representation.Configuration;

public final class StaticConfiguredNodeBooleanExpr extends NodeBooleanExpr {

   public StaticConfiguredNodeBooleanExpr(NodeExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Configuration node = _caller.evaluate(environment);
      return node.getStaticRoutes().size() > 0;
   }

}
