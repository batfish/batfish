package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

  protected String _routeMap;

  protected final RoutingProtocol _sourceProtocol;

  protected final Map<String, Object> _specialAttributes;

  public RedistributionPolicy(RoutingProtocol sourceProtocol) {
    _sourceProtocol = sourceProtocol;
    _specialAttributes = new TreeMap<>();
  }

  public String getRouteMap() {
    return _routeMap;
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
}
