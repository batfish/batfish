package org.batfish.representation.cumulus_interfaces;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Model of a Cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class Interfaces {
  private final Set<String> _autoIfaces = new HashSet<>();
  private final Map<String, Interface> _interfaces = new HashMap<>();

  /** Register an interface as being enabled on boot. */
  public void setAuto(String ifaceName) {
    _autoIfaces.add(ifaceName);
  }

  /** Create an interface with the following name, or return it if it already exists. */
  @Nonnull
  public Interface createOrGetInterface(String name) {
    Interface iface = _interfaces.get(name);
    if (iface == null) {
      iface = new Interface(name);
      _interfaces.put(name, iface);
    }
    return iface;
  }

  @Nonnull
  public Set<String> getAutoIfaces() {
    return _autoIfaces;
  }

  @Nonnull
  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }
}
