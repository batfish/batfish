package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;

public enum BgpNeighborBooleanExpr implements BooleanExpr {
   BGP_NEIGHBOR_HAS_GENERATED_ROUTE;

   @Override
   public Boolean evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = environment.getBgpNeighbor();
      switch (this) {

      case BGP_NEIGHBOR_HAS_GENERATED_ROUTE:
         return bgpNeighbor.getGeneratedRoutes().size() > 0;

      default:
         throw new BatfishException("invalid bgp neighbor boolean expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBooleanExpr.print(this, environment);
   }

}
