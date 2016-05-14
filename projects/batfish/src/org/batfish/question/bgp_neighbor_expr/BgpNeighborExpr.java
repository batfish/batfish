package org.batfish.question.bgp_neighbor_expr;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface BgpNeighborExpr extends Expr {

   @Override
   public BgpNeighbor evaluate(Environment environment);

}
