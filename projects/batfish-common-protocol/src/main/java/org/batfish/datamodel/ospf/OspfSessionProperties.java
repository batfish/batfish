package org.batfish.datamodel.ospf;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.IpLink;

/** Properties of an OSPF session that is compatible (based on two endpoint configurations). */
public class OspfSessionProperties {

  private IpLink _ipLink;

  public OspfSessionProperties(IpLink ipLink) {
    _ipLink = ipLink;
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
    return Objects.equals(_ipLink, that._ipLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipLink);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("ipLink", _ipLink).toString();
  }
}
