package org.batfish.question.node_expr.bgp_neighbor;

import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;

public final class OwnerBgpNeighborNodeExpr extends BgpNeighborNodeExpr {

   public OwnerBgpNeighborNodeExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Configuration evaluate(Environment environment) {
      BgpNeighbor caller = _caller.evaluate(environment);
      return caller.getOwner();

   }

}
