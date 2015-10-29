package org.batfish.question.int_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;

public enum BgpNeighborIntExpr implements IntExpr {
   LOCAL_AS,
   REMOTE_AS;

   @Override
   public Integer evaluate(Environment environment) {
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
      return BaseIntExpr.print(this, environment);
   }

}
