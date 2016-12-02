package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Ip6Prefix implements Prefix6Expr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip6Expr _ip6;

   private IntExpr _prefixLength;

   @JsonCreator
   public Ip6Prefix() {
   }

   public Ip6Prefix(Ip6Expr ip6, IntExpr prefixLength) {
      _ip6 = ip6;
      _prefixLength = prefixLength;
   }

   @Override
   public Prefix6 evaluate(Environment env) {
      Ip6 ip6 = _ip6.evaluate(env);
      int prefixLength = _prefixLength.evaluate(env);
      return new Prefix6(ip6, prefixLength);
   }

   public Ip6Expr getIp6() {
      return _ip6;
   }

   public IntExpr getPrefixLength() {
      return _prefixLength;
   }

   public void setIp6(Ip6Expr ip6) {
      _ip6 = ip6;
   }

   public void setPrefixLength(IntExpr prefixLength) {
      _prefixLength = prefixLength;
   }

}
