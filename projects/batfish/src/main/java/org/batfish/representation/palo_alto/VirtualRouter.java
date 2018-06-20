package org.batfish.representation.palo_alto;

import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;

public class VirtualRouter extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private final NavigableSet<String> _interfaceNames;

  private final Map<String, StaticRoute> _staticRoutes;

  public VirtualRouter(String name) {
    super(name);
    _interfaceNames = new TreeSet<>();
    _staticRoutes = new TreeMap<>();
  }

  public NavigableSet<String> getInterfaceNames() {
    return _interfaceNames;
  }

  public Map<String, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }
}
