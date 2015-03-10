package org.batfish.representation;

import java.io.Serializable;

import org.batfish.util.Util;

public class Prefix implements Comparable<Prefix>, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static final Prefix ZERO = new Prefix(new Ip(0l), 0);

   private static long getNetworkEnd(long networkStart, int prefix_length) {
      long networkEnd = networkStart;
      int ones_length = 32 - prefix_length;
      for (int i = 0; i < ones_length; i++) {
         networkEnd |= ((long) 1 << i);
      }
      return networkEnd;
   }

   private Ip _address;

   private int _prefixLength;

   public Prefix(Ip network, int prefixLength) {
      _address = network;
      _prefixLength = prefixLength;
   }

   public Prefix(Ip address, Ip mask) {
      _address = address;
      _prefixLength = mask.numSubnetBits();
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

   public boolean contains(Ip ip) {
      long start = getNetworkAddress().asLong();
      long end = getEndAddress().asLong();
      long ipAsLong = ip.asLong();
      return (start <= ipAsLong && ipAsLong <= end);
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
      return new Ip(getNetworkEnd(_address.asLong(), _prefixLength));
   }

   public Ip getNetworkAddress() {
      return _address.getNetworkAddress(_prefixLength);
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
      return Ip.numSubnetBitsToSubnetMask(_prefixLength);
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
