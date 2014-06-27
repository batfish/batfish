package batfish.representation;

import batfish.util.Util;

public class Ip implements Comparable<Ip> {

   private Long _ip;

   public Ip(long ipAsLong) {
      _ip = ipAsLong;
   }

   public Ip(String ipAsString) {
      _ip = Util.ipToLong(ipAsString);
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

   public Ip getWildcardEndIp(Ip wildcard) {
      return new Ip(_ip | wildcard.asLong());
   }

   @Override
   public int hashCode() {
      return _ip.hashCode();
   }

   @Override
   public String toString() {
      return Util.longToIp(_ip);
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
}
