package org.batfish.question.boolean_expr;

import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.question.ip_expr.IpExpr;
import org.batfish.representation.Ip;

public class ContainsIpExpr extends BaseBooleanExpr {

   private String _caller;

   private IpExpr _ipExpr;

   public ContainsIpExpr(String caller, IpExpr ipExpr) {
      _caller = caller;
      _ipExpr = ipExpr;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Set<Ip> ipSet = environment.getIpSets().get(_caller);
      Ip ip = _ipExpr.evaluate(environment);
      return ipSet.contains(ip);
   }

}
