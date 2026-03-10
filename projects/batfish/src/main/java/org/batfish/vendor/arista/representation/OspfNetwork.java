package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import org.batfish.datamodel.Prefix;

public class OspfNetwork implements Comparable<OspfNetwork>, Serializable {

  private long _area;
  private Prefix _prefix;

  public OspfNetwork(Prefix prefix, long area) {
    _prefix = prefix;
    _area = area;
  }

  @Override
  public int compareTo(OspfNetwork rhs) {
    int ret = _prefix.compareTo(rhs._prefix);
    if (ret == 0) {
      ret = Long.compare(_area, rhs._area);
    }
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfNetwork)) {
      return false;
    }
    OspfNetwork rhs = (OspfNetwork) o;
    return _prefix.equals(rhs._prefix) && _area == rhs._area;
  }

  public long getArea() {
    return _area;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + Long.hashCode(_area);
    result = prime * result + _prefix.hashCode();
    return result;
  }
}
