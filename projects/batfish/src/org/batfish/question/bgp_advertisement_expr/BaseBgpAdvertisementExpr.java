package org.batfish.question.bgp_advertisement_expr;

import org.batfish.question.Environment;

public abstract class BaseBgpAdvertisementExpr implements BgpAdvertisementExpr {

   public static String print(BgpAdvertisementExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
