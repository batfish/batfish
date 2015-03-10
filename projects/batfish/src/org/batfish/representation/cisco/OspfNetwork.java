package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Prefix;

public class OspfNetwork implements Comparable<OspfNetwork>, Serializable {

   private static final long serialVersionUID = 1L;

   private long _area;
   private int _hashCode;
   private Prefix _prefix;

   public OspfNetwork(Prefix prefix, long area) {
      _prefix = prefix;
      _area = area;
      _hashCode = (_prefix.toString() + ":" + _area).hashCode();
   }

   @Override
   public int compareTo(OspfNetwork rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = Long.compare(_area, rhs._area);
      }
      return ret;
   }

   @Override
   public boolean equals(Object o) {
      OspfNetwork rhs = (OspfNetwork) o;
      return _prefix.equals(rhs._prefix) && _area == rhs._area;
   }

   public long getArea() {
      return _area;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   @Override
   public int hashCode() {
      return _hashCode;
   }

}
