package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A Palo Alto virtual-router. Config entered at {@code network virtual-router NAME}. */
public class VirtualRouter implements Serializable {

  public VirtualRouter(String name) {
    _bgp = null;
    _interfaceNames = new TreeSet<>();
    _name = name;
    _staticRoutes = new TreeMap<>();
  }

  public @Nullable BgpVr getBgp() {
    return _bgp;
  }

  public @Nonnull BgpVr getOrCreateBgp() {
    if (_bgp == null) {
      _bgp = new BgpVr();
    }
    return _bgp;
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

  // Private implementation details

  private @Nullable BgpVr _bgp;
  private final NavigableSet<String> _interfaceNames;
  private final String _name;
  private final Map<String, StaticRoute> _staticRoutes;
}
