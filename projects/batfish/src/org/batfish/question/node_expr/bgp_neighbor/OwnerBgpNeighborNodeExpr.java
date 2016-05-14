package org.batfish.question.node_expr.bgp_neighbor;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;

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
