package org.batfish.question;

import java.util.Set;

import org.batfish.representation.Ip;

public class ContainsIpExpr implements BooleanExpr {

   private String _caller;

   private IpExpr _ipExpr;

   public ContainsIpExpr(String caller, IpExpr ipExpr) {
      _caller = caller;
      _ipExpr = ipExpr;
   }

   @Override
   public boolean evaluate(Environment environment) {
      Set<Ip> ipSet = environment.getIpSets().get(_caller);
      Ip ip = _ipExpr.evaluate(environment);
      return ipSet.contains(ip);
   }

}
