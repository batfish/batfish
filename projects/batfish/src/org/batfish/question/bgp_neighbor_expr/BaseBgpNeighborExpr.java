package org.batfish.question.bgp_neighbor_expr;

import org.batfish.question.Environment;

public abstract class BaseBgpNeighborExpr implements BgpNeighborExpr {

   public static String print(BgpNeighborExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
