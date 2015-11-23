package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;
import org.batfish.util.SubRange;

public final class Dependent implements Comparable<Dependent> {

   SubRange _lengthRange;

   Prefix _prefix;

   RoutingProtocol _protocol;

   public Dependent(RoutingProtocol protocol, Prefix prefix,
         SubRange lengthRange) {
      _protocol = protocol;
      _prefix = prefix;
      _lengthRange = lengthRange;
   }

   @Override
   public int compareTo(Dependent rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = _lengthRange.compareTo(rhs._lengthRange);
         if (ret == 0) {
            if (_protocol == null && rhs._protocol == null) {
               return 0;
            }
            else if (_protocol == null && rhs._protocol != null) {
               return -1;
            }
            else if (_protocol != null && rhs._protocol == null) {
               return 1;
            }
            else {
               ret = _protocol.compareTo(rhs._protocol);
            }
         }
      }
      return ret;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Dependent other = (Dependent) obj;
      if (!_lengthRange.equals(other._lengthRange)) {
         return false;
      }
      if (!_prefix.equals(other._prefix)) {
         return false;
      }
      if (_protocol != other._protocol) {
         return false;
      }
      return true;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _lengthRange.hashCode();
      result = prime * result + _prefix.hashCode();
      result = prime * result + _protocol.hashCode();
      return result;
   }

}
