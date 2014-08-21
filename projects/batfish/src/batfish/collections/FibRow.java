package batfish.collections;

import java.io.Serializable;

import batfish.representation.Ip;
import batfish.util.Util;

public class FibRow implements Comparable<FibRow>, Serializable {

   public static final String DROP_INTERFACE = "drop";

   private static final long serialVersionUID = 1L;

   private String _interface;
   private Ip _prefix;
   private int _prefixLength;

   public FibRow(Ip prefix, int prefixLength, String iface) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _interface = iface;
   }

   @Override
   public int compareTo(FibRow rhs) {
      int prefixComparison = _prefix.compareTo(rhs._prefix);
      if (prefixComparison == 0) {
         int lengthComparison = Integer.compare(_prefixLength,
               rhs._prefixLength);
         if (lengthComparison == 0) {
            return _interface.compareTo(rhs._interface);
         }
         else {
            return lengthComparison;
         }
      }
      else {
         return prefixComparison;
      }
   }

   @Override
   public boolean equals(Object o) {
      FibRow rhs = (FibRow) o;
      return (_prefix.equals(rhs._prefix) && _prefixLength == rhs._prefixLength && _interface
            .equals(rhs._interface));
   }

   public String getInterface() {
      return _interface;
   }

   public Ip getLastIp() {
      long prefix = _prefix.asLong();
      long lastIpAsLong = Util.getNetworkEnd(prefix, _prefixLength);
      return new Ip(lastIpAsLong);
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   @Override
   public int hashCode() {
      return (_prefix.toString() + _prefixLength).hashCode();
   }

   @Override
   public String toString() {
      return String.format("%-19s", _prefix.toString() + "/" + _prefixLength)
            + _interface;
   }

}
