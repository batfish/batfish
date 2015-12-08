package org.batfish.question.boolean_expr.node;

import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.representation.Configuration;

public final class IsisConfiguredNodeBooleanExpr extends NodeBooleanExpr {

   public IsisConfiguredNodeBooleanExpr(NodeExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Configuration node = _caller.evaluate(environment);
      return node.getIsisProcess() != null;
   }

}
