package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.Ip;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NextHopIp implements NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Ip> _ips;

   @JsonCreator
   public NextHopIp() {
   }

   public NextHopIp(List<Ip> ips) {
      _ips = ips;
   }

   public List<Ip> getIps() {
      return _ips;
   }

   public void setIps(List<Ip> ips) {
      _ips = ips;
   }

}
