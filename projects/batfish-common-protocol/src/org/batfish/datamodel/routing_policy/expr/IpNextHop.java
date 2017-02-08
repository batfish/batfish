package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IpNextHop extends NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Ip> _ips;

   @JsonCreator
   private IpNextHop() {
   }

   public IpNextHop(List<Ip> ips) {
      _ips = ips;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      IpNextHop other = (IpNextHop) obj;
      if (_ips == null) {
         if (other._ips != null) {
            return false;
         }
      }
      else if (!_ips.equals(other._ips)) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_ips == null) ? 0 : _ips.hashCode());
      return result;
   }

   public void setIps(List<Ip> ips) {
      _ips = ips;
   }

}
