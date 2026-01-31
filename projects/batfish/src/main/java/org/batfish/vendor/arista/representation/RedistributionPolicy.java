package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

  protected final RoutingProtocol _destinationProtocol;

  protected String _routeMap;

  protected final RedistributionSourceProtocol _sourceProtocol;

  protected final Map<String, Object> _specialAttributes;

  public RedistributionPolicy(
      RedistributionSourceProtocol sourceProtocol, RoutingProtocol destinationProtocol) {
    _sourceProtocol = sourceProtocol;
    _destinationProtocol = destinationProtocol;
    _specialAttributes = new TreeMap<>();
  }

  public RoutingProtocol getDestinationProtocol() {
    return _destinationProtocol;
  }

  public String getRouteMap() {
    return _routeMap;
  }

  public RedistributionSourceProtocol getSourceProtocol() {
    return _sourceProtocol;
  }

  public Map<String, Object> getSpecialAttributes() {
    return _specialAttributes;
  }

  public void setRouteMap(String routeMap) {
    _routeMap = routeMap;
  }
}
