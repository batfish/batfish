package org.batfish.question.ip_set_expr;

import org.batfish.question.Environment;

public abstract class BaseIpSetExpr implements IpSetExpr {

   public static String print(IpSetExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
