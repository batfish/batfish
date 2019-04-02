package org.batfish.datamodel.ospf;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.IpLink;

/** Properties of an OSPF session that is compatible (based on two endpoint configurations). */
public final class OspfSessionProperties {

  private final long _area;
  private final IpLink _ipLink;

  public OspfSessionProperties(long area, IpLink ipLink) {
    _area = area;
    _ipLink = ipLink;
  }

  public long getArea() {
    return _area;
  }

  public IpLink getIpLink() {
    return _ipLink;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfSessionProperties)) {
      return false;
    }
    OspfSessionProperties that = (OspfSessionProperties) o;
    return _area == that._area && Objects.equals(_ipLink, that._ipLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _ipLink);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("area", _area).add("ipLink", _ipLink).toString();
  }
}
