package org.batfish.question.boolean_expr.bgp_neighbor;

import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;

public class HasLocalIpBgpNeighborBooleanExpr extends BgpNeighborBooleanExpr {

   public HasLocalIpBgpNeighborBooleanExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      BgpNeighbor caller = _caller.evaluate(environment);
      return caller.getLocalIp() != null;
   }

}
