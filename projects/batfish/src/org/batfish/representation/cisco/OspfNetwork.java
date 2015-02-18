package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Ip;

public class OspfNetwork implements Comparable<OspfNetwork>, Serializable {

   private static final long serialVersionUID = 1L;

   private long _area;
   private int _hashCode;
   private Ip _networkAddress;
   private Ip _subnetMask;

   public OspfNetwork(Ip networkAddress, Ip subnetMask, long area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _area = area;
      _hashCode = (networkAddress.networkString(_subnetMask) + ":" + _area)
            .hashCode();
   }

   @Override
   public int compareTo(OspfNetwork rhs) {
      int ret = _networkAddress.compareTo(rhs._networkAddress);
      if (ret == 0) {
         ret = _subnetMask.compareTo(rhs._subnetMask);
         if (ret == 0) {
            ret = Long.compare(_area, rhs._area);
         }
      }
      return ret;
   }

   @Override
   public boolean equals(Object o) {
      OspfNetwork rhs = (OspfNetwork) o;
      return _networkAddress.equals(rhs._networkAddress)
            && _subnetMask.equals(rhs._subnetMask) && _area == rhs._area;
   }

   public long getArea() {
      return _area;
   }

   public Ip getNetworkAddress() {
      return _networkAddress;
   }

   public Ip getSubnetMask() {
      return _subnetMask;
   }

   @Override
   public int hashCode() {
      return _hashCode;
   }

}
