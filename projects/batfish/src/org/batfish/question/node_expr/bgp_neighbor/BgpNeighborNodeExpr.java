package org.batfish.question.node_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.node_expr.BaseNodeExpr;

public abstract class BgpNeighborNodeExpr extends BaseNodeExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborNodeExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
