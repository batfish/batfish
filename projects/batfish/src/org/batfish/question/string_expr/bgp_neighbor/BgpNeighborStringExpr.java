package org.batfish.question.string_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class BgpNeighborStringExpr extends BaseStringExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborStringExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
