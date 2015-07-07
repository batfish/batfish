package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.BgpNeighbor;

public enum BgpNeighborIntExpr implements IntExpr {
   LOCAL_AS,
   REMOTE_AS;

   @Override
   public int evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = environment.getBgpNeighbor();
      switch (this) {

      case LOCAL_AS:
         return bgpNeighbor.getLocalAs();

      case REMOTE_AS:
         return bgpNeighbor.getRemoteAs();

      default:
         throw new BatfishException("invalid bgp neighbor integer expression");
      }
   }

   @Override
   public String print(Environment environment) {
      return Integer.toString(evaluate(environment));
   }

}
