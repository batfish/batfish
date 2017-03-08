package org.batfish.datamodel;

import java.io.Serializable;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class Prefix implements Comparable<Prefix>, Serializable {

   public static final int MAX_PREFIX_LENGTH = 32;

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

   private static long numWildcardBitsToWildcardLong(int numBits) {
      long wildcard = 0;
      for (int i = 0; i < numBits; i++) {
         wildcard |= (1l << i);
      }
      return wildcard;
   }

   private Ip _address;

   private int _prefixLength;

   public Prefix(Ip address, int prefixLength) {
      if (address == null) {
         throw new BatfishException("Cannot create prefix with null network");
      }
      _address = address;
      _prefixLength = prefixLength;
   }

   public Prefix(Ip address, Ip mask) {
      if (address == null) {
         throw new BatfishException("Cannot create prefix with null network");
      }
      if (mask == null) {
         throw new BatfishException("Cannot create prefix with null mask");
      }
      _address = address;
      _prefixLength = mask.numSubnetBits();
   }

   @JsonCreator
   public Prefix(String text) {
      String[] parts = text.split("/");
      if (parts.length != 2) {
         throw new BatfishException("Invalid prefix string: \"" + text + "\"");
      }
      _address = new Ip(parts[0]);
      try {
         _prefixLength = Integer.parseInt(parts[1]);
      }
      catch (NumberFormatException e) {
         throw new BatfishException(
               "Invalid prefix length: \"" + parts[1] + "\"", e);
      }
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

   public boolean containsPrefix(Prefix prefix) {
      return contains(prefix._address) && _prefixLength < prefix._prefixLength;
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

   public Prefix getNetworkPrefix() {
      return new Prefix(getNetworkAddress(), _prefixLength);
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public Ip getPrefixWildcard() {
      int numWildcardBits = MAX_PREFIX_LENGTH - _prefixLength;
      long wildcardLong = numWildcardBitsToWildcardLong(numWildcardBits);
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
   @JsonValue
   public String toString() {
      return _address.toString() + "/" + _prefixLength;
   }

}
