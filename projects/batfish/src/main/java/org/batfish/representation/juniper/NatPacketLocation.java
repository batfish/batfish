package org.batfish.representation.juniper;

import java.io.Serializable;

public class NatPacketLocation implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private String _interface;

  private String _routingInstance;

  private String _zone;

  public String getInterface() {
    return _interface;
  }

  public String getRoutingInstance() {
    return _routingInstance;
  }

  public String getZone() {
    return _zone;
  }

  public void setInterface(String interfaceName) {
    _interface = interfaceName;
  }

  public void setRoutingInstance(String routingInstance) {
    _routingInstance = routingInstance;
  }

  public void setZone(String zone) {
    _zone = zone;
  }
}
