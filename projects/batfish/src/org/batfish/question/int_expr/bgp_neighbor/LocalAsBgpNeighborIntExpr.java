package org.batfish.question.int_expr.bgp_neighbor;

import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;

public final class LocalAsBgpNeighborIntExpr extends BgpNeighborIntExpr {

   public LocalAsBgpNeighborIntExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Integer evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = _caller.evaluate(environment);
      return bgpNeighbor.getLocalAs();
   }

}
