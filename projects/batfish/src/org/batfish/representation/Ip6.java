package org.batfish.representation;

import java.io.Serializable;
import java.math.BigInteger;

import org.batfish.common.BatfishException;

import com.google.common.net.InetAddresses;

public class Ip6 implements Comparable<Ip6>, Serializable {

   public static final Ip6 MAX = new Ip6(new BigInteger(
         "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16));

   private static final long serialVersionUID = 1L;

   public static final Ip6 ZERO = new Ip6(BigInteger.ZERO);

   private static String asIpv6AddressString(BigInteger ipv6AddressAsBigInteger) {
      BigInteger remainder = ipv6AddressAsBigInteger;
      String out = "";
      BigInteger segmentMask = new BigInteger("FFFF", 16);
      for (int i = 0; i < 8; i++) {
         out = remainder.and(segmentMask).toString(16) + ":" + out;
         remainder = remainder.shiftRight(16);
      }
      out = out.substring(0, out.length() - 1);
      return out;
   }

   private static BigInteger numSubnetBitsToSubnetBigInteger(int numBits) {
      BigInteger val = BigInteger.ZERO;
      for (int i = 127; i > 127 - numBits; i--) {
         val = val.or(BigInteger.ONE.shiftLeft(i));
      }
      return val;
   }

   private final int _hashCode;

   private final BigInteger _ip6;

   public Ip6(BigInteger ip6AsBigInteger) {
      _ip6 = ip6AsBigInteger;
      _hashCode = _ip6.hashCode();
   }

   public Ip6(String ipAsString) {
      boolean invalid = false;
      byte[] ip6AsByteArray = null;
      if (!ipAsString.contains(":")) {
         invalid = true;
      }
      else {
         try {
            ip6AsByteArray = InetAddresses.forString(ipAsString).getAddress();
         }
         catch (IllegalArgumentException e) {
            invalid = true;
         }
      }
      if (invalid) {
         throw new BatfishException("Invalid ipv6 address literal: \""
               + ipAsString + "\"");
      }
      _ip6 = new BigInteger(ip6AsByteArray);
      _hashCode = _ip6.hashCode();
   }

   public BigInteger asBigInteger() {
      return _ip6;
   }

   @Override
   public int compareTo(Ip6 rhs) {
      return _ip6.compareTo(rhs._ip6);
   }

   @Override
   public boolean equals(Object o) {
      Ip6 rhs = (Ip6) o;
      return _ip6.equals(rhs._ip6);
   }

   public Ip6 getNetworkAddress(int subnetBits) {
      BigInteger mask = numSubnetBitsToSubnetBigInteger(subnetBits);
      return new Ip6(_ip6.and(mask));
   }

   @Override
   public int hashCode() {
      return _hashCode;
   }

   public String networkString(int prefixLength) {
      return toString() + "/" + prefixLength;
   }

   @Override
   public String toString() {
      return asIpv6AddressString(_ip6);
   }

}
