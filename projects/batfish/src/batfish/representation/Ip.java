package batfish.representation;

import java.io.Serializable;

import batfish.util.Util;

public class Ip implements Comparable<Ip>, Serializable {

   private static final long serialVersionUID = 1L;

   private final int _hashCode;
   private final Long _ip;

   public Ip(long ipAsLong) {
      _ip = ipAsLong;
      _hashCode = _ip.hashCode();
   }

   public Ip(String ipAsString) {
      _ip = Util.ipToLong(ipAsString);
      _hashCode = _ip.hashCode();
   }

   public long asLong() {
      return _ip;
   }

   @Override
   public int compareTo(Ip rhs) {
      return Long.compare(_ip, rhs._ip);
   }

   @Override
   public boolean equals(Object o) {
      Ip rhs = (Ip) o;
      return _ip.equals(rhs._ip);
   }

   public Ip getClassMask() {
      long firstOctet = _ip >> 24;
      if (firstOctet <= 126) {
         return new Ip(0xFF000000l);
      }
      else if (firstOctet >= 128 && firstOctet <= 191) {
         return new Ip(0XFFFF0000l);
      }
      else if (firstOctet >= 192 && firstOctet <= 223) {
         return new Ip(0xFFFFFF00l);
      }
      else {
         return null;
      }
   }

   public Ip getNetworkAddress(Ip mask) {
      return new Ip(_ip & mask.asLong());
   }

   public Ip getSubnetEnd(Ip mask) {
      return new Ip(_ip | mask.inverted().asLong());
   }

   public Ip getWildcardEndIp(Ip wildcard) {
      return new Ip(_ip | wildcard.asLong());
   }

   @Override
   public int hashCode() {
      return _hashCode;
   }

   public Ip inverted() {
      long invertedLong = (~_ip) & 0xFFFFFFFFl;
      return new Ip(invertedLong);
   }

   public String networkString(int prefixLength) {
      return toString() + "/" + prefixLength;
   }

   public String networkString(Ip mask) {
      return toString() + "/" + mask.numSubnetBits();
   }

   public int numSubnetBits() {
      int count = 0;
      int subnetInt = (int) _ip.longValue();
      while (subnetInt != 0) {
         subnetInt <<= 1;
         count++;
      }
      return count;
   }

   @Override
   public String toString() {
      return Util.longToIp(_ip);
   }
}
