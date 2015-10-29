package org.batfish.question.ip_expr;

import org.batfish.question.Environment;

public abstract class BaseIpExpr implements IpExpr {

   public static final String print(IpExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
