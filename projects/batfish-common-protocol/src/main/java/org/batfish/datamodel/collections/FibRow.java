package org.batfish.datamodel.collections;

import java.io.Serializable;
import org.batfish.datamodel.Prefix;

public class FibRow implements Comparable<FibRow>, Serializable {

  public static final String DROP_INTERFACE = "drop";

  private static final long serialVersionUID = 1L;

  private String _interface;
  private String _nextHop;
  private String _nextHopInterface;
  private Prefix _prefix;

  public FibRow(Prefix prefix, String iface, String nextHop, String nextHopInterface) {
    _prefix = prefix;
    _interface = iface;
    _nextHop = nextHop;
    _nextHopInterface = nextHopInterface;
  }

  @Override
  public int compareTo(FibRow rhs) {
    int prefixComparison = _prefix.getStartIp().compareTo(rhs._prefix.getStartIp());
    if (prefixComparison == 0) {
      int lengthComparison =
          Integer.compare(_prefix.getPrefixLength(), rhs._prefix.getPrefixLength());
      if (lengthComparison == 0) {
        int interfaceComparison = _interface.compareTo(rhs._interface);
        if (interfaceComparison == 0) {
          int nextHopComparison = _nextHop.compareTo(rhs._nextHop);
          if (nextHopComparison == 0) {
            return _nextHopInterface.compareTo(rhs._nextHopInterface);
          } else {
            return nextHopComparison;
          }
        } else {
          return interfaceComparison;
        }
      } else {
        return lengthComparison;
      }
    } else {
      return prefixComparison;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof FibRow)) {
      return false;
    }
    FibRow rhs = (FibRow) o;
    return _prefix.equals(rhs._prefix) && _interface.equals(rhs._interface);
  }

  public String getInterface() {
    return _interface;
  }

  public String getNextHop() {
    return _nextHop;
  }

  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _interface.hashCode();
    result = prime * result + _prefix.hashCode();
    result = prime * result + _nextHop.hashCode();
    result = prime * result + _nextHopInterface.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("%-19s", _prefix) + _interface;
  }
}
