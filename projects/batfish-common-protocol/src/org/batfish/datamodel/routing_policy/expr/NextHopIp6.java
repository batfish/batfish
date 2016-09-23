package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NextHopIp6 implements NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Ip6> _ips;

   @JsonCreator
   public NextHopIp6() {
   }

   public NextHopIp6(List<Ip6> ips) {
      _ips = ips;
   }

   public List<Ip6> getIps() {
      return _ips;
   }

   @Override
   public Ip getNextHopIp() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setIps(List<Ip6> ips) {
      _ips = ips;
   }

}
