package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Ip6NextHop implements NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Ip6> _ips;

   @JsonCreator
   public Ip6NextHop() {
   }

   public Ip6NextHop(List<Ip6> ips) {
      _ips = ips;
   }

   public List<Ip6> getIps() {
      return _ips;
   }

   @Override
   public Ip getNextHopIp(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setIps(List<Ip6> ips) {
      _ips = ips;
   }

}
