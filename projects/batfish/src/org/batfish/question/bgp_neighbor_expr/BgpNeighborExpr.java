package org.batfish.question.bgp_neighbor_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.BgpNeighbor;

public interface BgpNeighborExpr extends Expr {

   @Override
   public BgpNeighbor evaluate(Environment environment);

}
