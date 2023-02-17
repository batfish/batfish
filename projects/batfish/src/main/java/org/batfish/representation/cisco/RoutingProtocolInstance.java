package org.batfish.representation.cisco;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.RoutingProtocol;

public class RoutingProtocolInstance implements Serializable {
  public static RoutingProtocolInstance bgp() {
    return new RoutingProtocolInstance(RoutingProtocol.BGP);
  }

  public static RoutingProtocolInstance connected() {
    return new RoutingProtocolInstance(RoutingProtocol.CONNECTED);
  }

  public static RoutingProtocolInstance eigrp() {
    return new RoutingProtocolInstance(RoutingProtocol.EIGRP);
  }

  public static RoutingProtocolInstance ospf() {
    return new RoutingProtocolInstance(RoutingProtocol.OSPF);
  }

  public static RoutingProtocolInstance rip() {
    return new RoutingProtocolInstance(RoutingProtocol.RIP);
  }

  public static RoutingProtocolInstance isis_l1() {
    return new RoutingProtocolInstance(RoutingProtocol.ISIS_L1);
  }

  public static RoutingProtocolInstance staticRoutingProtocol() {
    return new RoutingProtocolInstance(RoutingProtocol.STATIC);
  }

  private final @Nonnull RoutingProtocol _protocol;

  public RoutingProtocolInstance(RoutingProtocol protocol) {
    _protocol = protocol;
  }

  public @Nonnull RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RoutingProtocolInstance)) {
      return false;
    }
    RoutingProtocolInstance that = (RoutingProtocolInstance) o;
    return _protocol == that._protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocol);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues().add("protocol", _protocol).toString();
  }
}
