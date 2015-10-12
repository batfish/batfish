package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.Prefix;
import org.batfish.representation.StaticRoute;

public enum StaticRoutePrefixExpr implements PrefixExpr {
   STATICROUTE_PREFIX;

   @Override
   public Prefix evaluate(Environment environment) {
      StaticRoute staticRoute = environment.getStaticRoute();
      switch (this) {

      case STATICROUTE_PREFIX:
         return staticRoute.getPrefix();

      default:
         throw new BatfishException("invalid staticroute prefix expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return evaluate(environment).toString();
   }

}
