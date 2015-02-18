package org.batfish.representation;

import java.io.Serializable;

import org.batfish.util.Util;

public class Prefix implements Comparable<Prefix>, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static final Prefix ZERO = new Prefix(new Ip(0l), 0);

   private Ip _address;
   private int _prefixLength;

   public Prefix(Ip network, int prefixLength) {
      _address = network;
      _prefixLength = prefixLength;
   }

   public Prefix(String text) {
      String[] parts = text.split("/");
      _address = new Ip(parts[0]);
      _prefixLength = Integer.parseInt(parts[1]);
   }

   @Override
   public int compareTo(Prefix rhs) {
      int ret = _address.compareTo(rhs._address);
      if (ret != 0) {
         return ret;
      }
      return Integer.compare(_prefixLength, rhs._prefixLength);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Prefix rhs = (Prefix) obj;
      return _address.equals(rhs._address)
            && _prefixLength == rhs._prefixLength;
   }

   public Ip getAddress() {
      return _address;
   }

   public Ip getEndAddress() {
      return new Ip(Util.getNetworkEnd(_address.asLong(), _prefixLength));
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public Ip getPrefixWildcard() {
      int numWildcardBits = 32 - _prefixLength;
      long wildcardLong = Util.numWildcardBitsToWildcardLong(numWildcardBits);
      return new Ip(wildcardLong);
   }

   public Ip getSubnetMask() {
      return new Ip(Util.numSubnetBitsToSubnetLong(_prefixLength));
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _address.hashCode();
      result = prime * result + _prefixLength;
      return result;
   }

   @Override
   public String toString() {
      return _address.toString() + "/" + _prefixLength;
   }
}
