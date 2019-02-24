package org.batfish.dataplane.traceroute;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.Ip;

/**
 * A flow exit point out of a device. A combination of the IP to arp for and the interface name to
 * use.
 */
final class ExitPoint {
  private final Ip _arpIp;
  private final String _interfaceName;

  ExitPoint(Ip arpIp, String interfaceName) {
    _arpIp = arpIp;
    _interfaceName = interfaceName;
  }

  static ExitPoint from(FibEntry fibEntry) {
    return new ExitPoint(fibEntry.getArpIP(), fibEntry.getInterfaceName());
  }

  public Ip getArpIP() {
    return _arpIp;
  }

  public String getInterfaceName() {
    return _interfaceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExitPoint exitPoint = (ExitPoint) o;
    return Objects.equals(_arpIp, exitPoint._arpIp)
        && Objects.equals(_interfaceName, exitPoint._interfaceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arpIp, _interfaceName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("arpIp", _arpIp)
        .add("interfaceName", _interfaceName)
        .toString();
  }
}
