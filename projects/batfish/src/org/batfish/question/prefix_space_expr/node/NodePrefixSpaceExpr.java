package org.batfish.question.prefix_space_expr.node;

import org.batfish.question.node_expr.NodeExpr;
import org.batfish.question.prefix_space_expr.BasePrefixSpaceExpr;

public abstract class NodePrefixSpaceExpr extends BasePrefixSpaceExpr {

   protected final NodeExpr _caller;

   public NodePrefixSpaceExpr(NodeExpr caller) {
      _caller = caller;
   }

}
