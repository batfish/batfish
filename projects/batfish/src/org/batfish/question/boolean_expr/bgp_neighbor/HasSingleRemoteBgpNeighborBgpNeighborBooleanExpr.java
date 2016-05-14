package org.batfish.question.boolean_expr.bgp_neighbor;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;

public class HasSingleRemoteBgpNeighborBgpNeighborBooleanExpr extends
      BgpNeighborBooleanExpr {

   public HasSingleRemoteBgpNeighborBgpNeighborBooleanExpr(
         BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      BgpNeighbor caller = _caller.evaluate(environment);
      environment.initRemoteBgpNeighbors();
      return caller.getCandidateRemoteBgpNeighbors().size() == 1;
   }

}
