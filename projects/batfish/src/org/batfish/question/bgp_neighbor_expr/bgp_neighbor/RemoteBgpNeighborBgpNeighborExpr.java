package org.batfish.question.bgp_neighbor_expr.bgp_neighbor;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;

public final class RemoteBgpNeighborBgpNeighborExpr extends
      BgpNeighborBgpNeighborExpr {

   public RemoteBgpNeighborBgpNeighborExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public BgpNeighbor evaluate(Environment environment) {
      BgpNeighbor caller = _caller.evaluate(environment);
      environment.initRemoteBgpNeighbors();
      return caller.getRemoteBgpNeighbor();
   }

}
