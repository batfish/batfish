package org.batfish.question.boolean_expr.node;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.node_expr.NodeExpr;

public abstract class NodeBooleanExpr extends BaseBooleanExpr {

   protected final NodeExpr _caller;

   public NodeBooleanExpr(NodeExpr caller) {
      _caller = caller;
   }

}
