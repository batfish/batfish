package org.batfish.question;

import org.batfish.common.BatfishException;
import org.batfish.question.prefix_expr.PrefixExpr;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Prefix;

public enum GeneratedRoutePrefixExpr implements PrefixExpr {
   GENERATED_ROUTE_PREFIX;

   @Override
   public Prefix evaluate(Environment environment) {
      GeneratedRoute generatedRoute = environment.getGeneratedRoute();
      switch (this) {

      case GENERATED_ROUTE_PREFIX:
         return generatedRoute.getPrefix();

      default:
         throw new BatfishException("invalid generated route prefix expr");

      }
   }

   @Override
   public String print(Environment environment) {
      return evaluate(environment).toString();
   }

}
