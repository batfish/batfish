package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Ip6NextHop extends NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Ip6> _ips;

   @JsonCreator
   private Ip6NextHop() {
   }

   public Ip6NextHop(List<Ip6> ips) {
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
      Ip6NextHop other = (Ip6NextHop) obj;
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

   public List<Ip6> getIps() {
      return _ips;
   }

   @Override
   public Ip getNextHopIp(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_ips == null) ? 0 : _ips.hashCode());
      return result;
   }

   public void setIps(List<Ip6> ips) {
      _ips = ips;
   }

}
