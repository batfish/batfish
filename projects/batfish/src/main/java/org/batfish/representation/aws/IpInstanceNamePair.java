package org.batfish.representation.aws;

import java.util.Objects;
import org.batfish.datamodel.Ip;

/** Pair of interface IP and the name of the containing instance */
public class IpInstanceNamePair {
  private Ip _ip;
  private String _instanceName;

  public IpInstanceNamePair(Ip ip, String instanceName) {
    _ip = ip;
    _instanceName = instanceName;
  }

  public Ip getIp() {
    return _ip;
  }

  public String getInstanceName() {
    return _instanceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpInstanceNamePair)) {
      return false;
    }
    IpInstanceNamePair that = (IpInstanceNamePair) o;
    return Objects.equals(_ip, that._ip) && Objects.equals(_instanceName, that._instanceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _instanceName);
  }
}
