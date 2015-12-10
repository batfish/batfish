package org.batfish.question.boolean_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.boolean_expr.BaseBooleanExpr;

public abstract class BgpNeighborBooleanExpr extends BaseBooleanExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborBooleanExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
