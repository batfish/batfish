package org.batfish.question.ip_expr.bgp_neighbor;

import org.batfish.common.datamodel.Ip;
import org.batfish.question.Environment;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.representation.BgpNeighbor;

public final class RemoteIpBgpNeighborIpExpr extends BgpNeighborIpExpr {

   public RemoteIpBgpNeighborIpExpr(BgpNeighborExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      BgpNeighbor bgpNeighbor = _caller.evaluate(environment);
      return bgpNeighbor.getAddress();
   }

}
