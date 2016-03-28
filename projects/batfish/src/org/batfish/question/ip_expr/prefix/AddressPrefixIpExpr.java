package org.batfish.question.ip_expr.prefix;

import org.batfish.question.Environment;
import org.batfish.question.prefix_expr.PrefixExpr;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;

public final class AddressPrefixIpExpr extends PrefixIpExpr {

   public AddressPrefixIpExpr(PrefixExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      Prefix caller = _caller.evaluate(environment);
      return caller.getAddress();
   }

}
