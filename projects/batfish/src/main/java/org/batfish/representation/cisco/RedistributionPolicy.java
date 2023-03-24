package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;

public abstract class RedistributionPolicy implements Serializable {

  protected String _routeMap;

  private final @Nonnull RoutingProtocolInstance _instance;

  protected final Map<String, Object> _specialAttributes;

  public RedistributionPolicy(RoutingProtocolInstance instance) {
    _instance = instance;
    _specialAttributes = new TreeMap<>();
  }

  public String getRouteMap() {
    return _routeMap;
  }

  public @Nonnull RoutingProtocolInstance getInstance() {
    return _instance;
  }

  public Map<String, Object> getSpecialAttributes() {
    return _specialAttributes;
  }

  public void setRouteMap(String routeMap) {
    _routeMap = routeMap;
  }
}
