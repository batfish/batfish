package org.batfish.question.boolean_expr.bgp_neighbor;

import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;

public class HasRemoteBgpNeighborBgpNeighborBooleanExpr extends
      BgpNeighborBooleanExpr {

   public HasRemoteBgpNeighborBgpNeighborBooleanExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      BgpNeighbor caller = _caller.evaluate(environment);
      environment.initRemoteBgpNeighbors();
      return caller.getRemoteBgpNeighbor() != null;
   }

}
