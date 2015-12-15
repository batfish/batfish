package org.batfish.question.bgp_neighbor_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BaseBgpNeighborExpr;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;

public abstract class BgpNeighborBgpNeighborExpr extends BaseBgpNeighborExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborBgpNeighborExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
