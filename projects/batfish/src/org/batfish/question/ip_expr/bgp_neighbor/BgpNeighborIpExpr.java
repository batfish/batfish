package org.batfish.question.ip_expr.bgp_neighbor;

import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.ip_expr.BaseIpExpr;

public abstract class BgpNeighborIpExpr extends BaseIpExpr {

   protected final BgpNeighborExpr _caller;

   public BgpNeighborIpExpr(BgpNeighborExpr caller) {
      _caller = caller;
   }

}
