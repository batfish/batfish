package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents how packets enter and exit a Nat */
public final class NatPacketLocation implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nullable private String _interface;

  @Nullable private String _routingInstance;

  @Nullable private String _zone;

  @Nullable
  public String getInterface() {
    return _interface;
  }

  @Nullable
  public String getRoutingInstance() {
    return _routingInstance;
  }

  @Nullable
  public String getZone() {
    return _zone;
  }

  public void setInterface(@Nullable String interfaceName) {
    _interface = interfaceName;
  }

  public void setRoutingInstance(@Nullable String routingInstance) {
    _routingInstance = routingInstance;
  }

  public void setZone(@Nullable String zone) {
    _zone = zone;
  }
}
