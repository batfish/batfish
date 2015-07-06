package org.batfish.question;

import org.batfish.representation.Ip;

public abstract class BaseIpExpr implements IpExpr {

   @Override
   public abstract Ip evaluate(Environment environment);

   @Override
   public final String print(Environment environment) {
      return evaluate(environment).toString();
   }

}
