package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

  protected String _routeMap;

  protected final RoutingProtocol _sourceProtocol;

  protected final Map<String, List<Object>> _specialAttributes;

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

  public Map<String, List<Object>> getSpecialAttributes() {
    return _specialAttributes;
  }

  public void setRouteMap(String routeMap) {
    _routeMap = routeMap;
  }
}
