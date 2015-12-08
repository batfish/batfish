package org.batfish.question.bgp_neighbor_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;

public final class VarBgpNeighborExpr extends BaseBgpNeighborExpr {

   private final String _variable;

   public VarBgpNeighborExpr(String variable) {
      _variable = variable;
   }

   @Override
   public BgpNeighbor evaluate(Environment environment) {
      BgpNeighbor value = environment.getBgpNeighbors().get(_variable);
      if (value == null) {
         throw new BatfishException(
               "Reference to undefined bgp_neighbor variable: \"" + _variable
                     + "\"");
      }
      else {
         return value;
      }
   }

}
