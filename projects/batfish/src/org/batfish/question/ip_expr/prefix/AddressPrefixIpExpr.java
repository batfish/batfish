package org.batfish.question.ip_expr.prefix;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.prefix_expr.PrefixExpr;

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
