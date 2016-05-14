package org.batfish.question.int_expr.bgp_neighbor;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;

public final class RemoteAsBgpNeighborIntExpr extends BgpNeighborIntExpr {

   public RemoteAsBgpNeighborIntExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Integer evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = _caller.evaluate(environment);
      return bgpNeighbor.getRemoteAs();
   }

}
