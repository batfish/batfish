package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Ip;

public class OspfWildcardNetwork implements Comparable<OspfWildcardNetwork>,
      Serializable {

   private static final long serialVersionUID = 1L;

   private long _area;
   private int _hashCode;
   private Ip _prefix;
   private Ip _wildcard;

   public OspfWildcardNetwork(Ip prefix, Ip wildcard, long area) {
      _prefix = prefix;
      _wildcard = wildcard;
      _area = area;
      _hashCode = (prefix.networkString(_wildcard) + ":" + _area).hashCode();
   }

   @Override
   public int compareTo(OspfWildcardNetwork rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = _wildcard.compareTo(rhs._wildcard);
         if (ret == 0) {
            ret = Long.compare(_area, rhs._area);
         }
      }
      return ret;
   }

   @Override
   public boolean equals(Object o) {
      OspfWildcardNetwork rhs = (OspfWildcardNetwork) o;
      return _prefix.equals(rhs._prefix) && _wildcard.equals(rhs._wildcard)
            && _area == rhs._area;
   }

   public long getArea() {
      return _area;
   }

   public Ip getNetworkAddress() {
      return _prefix;
   }

   public Ip getWildcard() {
      return _wildcard;
   }

   @Override
   public int hashCode() {
      return _hashCode;
   }

}
