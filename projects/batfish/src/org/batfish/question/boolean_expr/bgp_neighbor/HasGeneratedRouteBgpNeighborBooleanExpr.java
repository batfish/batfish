package org.batfish.question.boolean_expr.bgp_neighbor;

import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;

public class HasGeneratedRouteBgpNeighborBooleanExpr extends
      BgpNeighborBooleanExpr {

   public HasGeneratedRouteBgpNeighborBooleanExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = _caller.evaluate(environment);
      return bgpNeighbor.getGeneratedRoutes().size() > 0;
   }

}
