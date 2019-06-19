package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

  private static final long serialVersionUID = 1L;

  protected final RoutingProtocol _destinationProtocol;

  protected String _routeMap;

  protected Integer _routeMapLine;

  protected final RoutingProtocol _sourceProtocol;

  protected final Map<String, Object> _specialAttributes;

  public RedistributionPolicy(RoutingProtocol sourceProtocol, RoutingProtocol destinationProtocol) {
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

  public Integer getRouteMapLine() {
    return _routeMapLine;
  }

  public RoutingProtocol getSourceProtocol() {
    return _sourceProtocol;
  }

  public Map<String, Object> getSpecialAttributes() {
    return _specialAttributes;
  }

  public void setRouteMap(String routeMap) {
    _routeMap = routeMap;
  }

  public void setRouteMapLine(Integer routeMapLine) {
    _routeMapLine = routeMapLine;
  }
}
