package org.batfish.question.bgp_neighbor_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;

public enum BaseCaseBgpNeighborExpr implements BgpNeighborExpr {
   BGP_NEIGHBOR;

   @Override
   public BgpNeighbor evaluate(Environment environment) {
      switch (this) {
      case BGP_NEIGHBOR:
         return environment.getBgpNeighbor();

      default:
         throw new BatfishException("Invalid "
               + this.getClass().getSimpleName());
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBgpNeighborExpr.print(this, environment);
   }

}
