package org.batfish.question;

import org.batfish.common.BatfishException;
import org.batfish.representation.StaticRoute;

public enum StaticRouteIntExpr implements IntExpr {
   STATICROUTE_ADMINISTRATIVE_COST;

   @Override
   public int evaluate(Environment environment) {
      StaticRoute staticRoute = environment.getStaticRoute();
      switch (this) {

      case STATICROUTE_ADMINISTRATIVE_COST:
         return staticRoute.getAdministrativeCost();

      default:
         throw new BatfishException("invalid staticroute int expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return Integer.toString(evaluate(environment));
   }

}
