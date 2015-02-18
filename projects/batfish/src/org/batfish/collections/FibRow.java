package org.batfish.collections;

import java.io.Serializable;

import org.batfish.representation.Ip;
import org.batfish.util.Util;

public class FibRow implements Comparable<FibRow>, Serializable {

   public static final String DROP_INTERFACE = "drop";

   private static final long serialVersionUID = 1L;

   private String _interface;
   private String _nextHop;
   private String _nextHopInterface;
   private Ip _prefix;
   private int _prefixLength;

   public FibRow(Ip prefix, int prefixLength, String iface, String nextHop,
         String nextHopInterface) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _interface = iface;
      _nextHop = nextHop;
      _nextHopInterface = nextHopInterface;
   }

   @Override
   public int compareTo(FibRow rhs) {
      int prefixComparison = _prefix.compareTo(rhs._prefix);
      if (prefixComparison == 0) {
         int lengthComparison = Integer.compare(_prefixLength,
               rhs._prefixLength);
         if (lengthComparison == 0) {
            int interfaceComparison = _interface.compareTo(rhs._interface);
            if (interfaceComparison == 0) {
               int nextHopComparison = _nextHop.compareTo(rhs._nextHop);
               if (nextHopComparison == 0) {
                  return _nextHopInterface.compareTo(rhs._nextHopInterface);
               }
               else {
                  return nextHopComparison;
               }
            }
            else {
               return interfaceComparison;
            }
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

   public String getNextHop() {
      return _nextHop;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _interface.hashCode();
      result = prime * result + _prefix.hashCode();
      result = prime * result + _nextHop.hashCode();
      result = prime * result + _nextHopInterface.hashCode();
      result = prime * result + _prefixLength;
      return result;
   }

   @Override
   public String toString() {
      return String.format("%-19s", _prefix.toString() + "/" + _prefixLength)
            + _interface;
   }

}
