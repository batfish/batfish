package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.BgpNeighbor;

public enum BgpNeighborBooleanExpr implements BooleanExpr {
   BGP_NEIGHBOR_HAS_GENERATED_ROUTE;

   @Override
   public boolean evaluate(Environment environment) {
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
      return Boolean.toString(evaluate(environment));
   }

}
