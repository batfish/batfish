package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class OspfWildcardNetwork implements Comparable<OspfWildcardNetwork>, Serializable {

  private long _area;
  private Ip _prefix;
  private Ip _wildcard;

  public OspfWildcardNetwork(Ip prefix, Ip wildcard, long area) {
    _prefix = prefix;
    _wildcard = wildcard;
    _area = area;
  }

  @Override
  public int compareTo(OspfWildcardNetwork rhs) {
    int ret = _prefix.compareTo(rhs._prefix);
    if (ret == 0) {
      ret = _wildcard.compareTo(rhs._wildcard);
      if (ret == 0) {
        ret = Long.compare(_area, rhs._area);
      }
    }
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfWildcardNetwork)) {
      return false;
    }
    OspfWildcardNetwork rhs = (OspfWildcardNetwork) o;
    return _prefix.equals(rhs._prefix) && _wildcard.equals(rhs._wildcard) && _area == rhs._area;
  }

  public long getArea() {
    return _area;
  }

  public Ip getNetworkAddress() {
    return _prefix;
  }

  public Ip getWildcard() {
    return _wildcard;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + _prefix.hashCode();
    result = prime * result + _wildcard.hashCode();
    result = prime * result + Long.hashCode(_area);
    return result;
  }
}
