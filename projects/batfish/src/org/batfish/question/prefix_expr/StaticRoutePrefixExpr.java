package org.batfish.question.prefix_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
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
      return BasePrefixExpr.print(this, environment);
   }

}
