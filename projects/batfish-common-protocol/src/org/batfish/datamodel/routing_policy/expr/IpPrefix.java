package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IpPrefix implements PrefixExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IpExpr _ip;

   private IntExpr _prefixLength;

   @JsonCreator
   public IpPrefix() {
   }

   public IpPrefix(IpExpr ip, IntExpr prefixLength) {
      _ip = ip;
      _prefixLength = prefixLength;
   }

   @Override
   public Prefix evaluate(Environment env) {
      Ip ip = _ip.evaluate(env);
      int prefixLength = _prefixLength.evaluate(env);
      return new Prefix(ip, prefixLength);
   }

   public IpExpr getIp() {
      return _ip;
   }

   public IntExpr getPrefixLength() {
      return _prefixLength;
   }

   public void setIp(IpExpr ip) {
      _ip = ip;
   }

   public void setPrefixLength(IntExpr prefixLength) {
      _prefixLength = prefixLength;
   }

}
