package org.batfish.question.interface_expr;

import org.batfish.question.Environment;

public abstract class BaseInterfaceExpr implements InterfaceExpr {

   public static final String print(InterfaceExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
