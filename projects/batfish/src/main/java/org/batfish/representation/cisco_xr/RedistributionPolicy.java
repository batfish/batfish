package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

  protected final RoutingProtocol _destinationProtocol;

  protected @Nullable String _routeMap;

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

  public @Nullable String getRouteMap() {
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
