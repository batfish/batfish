package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

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

   @Override
   public Ip getNextHopIp(Environment environment) {
      if (_ips.size() == 1) {
         return _ips.get(0);
      }
      else {
         throw new BatfishException(
               "Do not currently support setting more than 1 next-hop-ip");
      }
   }

   public void setIps(List<Ip> ips) {
      _ips = ips;
   }

}
