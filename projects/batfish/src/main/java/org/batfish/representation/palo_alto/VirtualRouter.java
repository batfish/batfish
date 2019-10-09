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
    _adminDists = new AdminDistances();
    _bgp = null;
    _interfaceNames = new TreeSet<>();
    _name = name;
    _ospf = null;
    _staticRoutes = new TreeMap<>();
  }

  public @Nonnull AdminDistances getAdminDists() {
    return _adminDists;
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

  @Nullable
  public OspfVr getOspf() {
    return _ospf;
  }

  public @Nonnull OspfVr getOrCreateOspf() {
    if (_ospf == null) {
      _ospf = new OspfVr();
    }
    return _ospf;
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

  private @Nonnull final AdminDistances _adminDists;
  private @Nullable BgpVr _bgp;
  private final NavigableSet<String> _interfaceNames;
  private final String _name;
  private @Nullable OspfVr _ospf;
  private final Map<String, StaticRoute> _staticRoutes;
}
