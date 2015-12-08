package org.batfish.question.int_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.int_expr.BaseIntExpr;

public abstract class BgpNeighborIntExpr extends BaseIntExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborIntExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
