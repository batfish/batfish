package org.batfish.representation;

import java.io.Serializable;
import java.math.BigInteger;

import org.batfish.common.BatfishException;

public class Prefix6 implements Comparable<Prefix6>, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static final Prefix6 ZERO = new Prefix6(Ip6.ZERO, 0);

   private static BigInteger getNetworkEnd(BigInteger networkStart,
         int prefix_length) {
      BigInteger networkEnd = networkStart;
      int ones_length = 128 - prefix_length;
      for (int i = 0; i < ones_length; i++) {
         networkEnd = networkEnd.or(BigInteger.ONE.shiftLeft(i));
      }
      return networkEnd;
   }

   private Ip6 _address;

   private int _prefixLength;

   public Prefix6(Ip6 network, int prefixLength) {
      _address = network;
      _prefixLength = prefixLength;
   }

   public Prefix6(String text) {
      String[] parts = text.split("/");
      if (parts.length != 2) {
         throw new BatfishException("Invalid Prefix6 string: \"" + text + "\"");
      }
      _address = new Ip6(parts[0]);
      try {
         _prefixLength = Integer.parseInt(parts[1]);
      }
      catch (NumberFormatException e) {
         throw new BatfishException("Invalid Prefix6 length: \"" + parts[1]
               + "\"", e);
      }
   }

   @Override
   public int compareTo(Prefix6 rhs) {
      int ret = _address.compareTo(rhs._address);
      if (ret != 0) {
         return ret;
      }
      return Integer.compare(_prefixLength, rhs._prefixLength);
   }

   public boolean contains(Ip6 Ip6) {
      BigInteger start = getNetworkAddress().asBigInteger();
      BigInteger end = getEndAddress().asBigInteger();
      BigInteger ipAsLong = Ip6.asBigInteger();
      return (start.compareTo(ipAsLong) <= 0 && ipAsLong.compareTo(end) <= 0);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Prefix6 rhs = (Prefix6) obj;
      return _address.equals(rhs._address)
            && _prefixLength == rhs._prefixLength;
   }

   public Ip6 getAddress() {
      return _address;
   }

   public Ip6 getEndAddress() {
      return new Ip6(getNetworkEnd(_address.asBigInteger(), _prefixLength));
   }

   public Ip6 getNetworkAddress() {
      return _address.getNetworkAddress(_prefixLength);
   }

   public int getPrefixLength() {
      return _prefixLength;
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
