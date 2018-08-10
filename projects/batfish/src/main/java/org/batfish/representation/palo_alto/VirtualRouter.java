package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class VirtualRouter implements Serializable {

  private static final long serialVersionUID = 1L;

  private final NavigableSet<String> _interfaceNames;

  private final String _name;

  private final Map<String, StaticRoute> _staticRoutes;

  public VirtualRouter(String name) {
    _interfaceNames = new TreeSet<>();
    _name = name;
    _staticRoutes = new TreeMap<>();
  }

  public NavigableSet<String> getInterfaceNames() {
    return _interfaceNames;
  }

  public String getName() {
    return _name;
  }

  public Map<String, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }
}
