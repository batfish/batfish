package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.InterfaceConverter.isVrf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Model of a Cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class CumulusInterfacesConfiguration implements Serializable {
  @Nonnull private final Set<String> _autoIfaces;
  @Nonnull private final Map<String, InterfacesInterface> _interfaces;

  public CumulusInterfacesConfiguration() {
    _autoIfaces = new HashSet<>();
    _interfaces = new HashMap<>();
  }

  /** Register an interface as being enabled on boot. */
  public void setAuto(String ifaceName) {
    _autoIfaces.add(ifaceName);
  }

  /** Create an interface with the following name, or return it if it already exists. */
  @Nonnull
  public InterfacesInterface createOrGetInterface(String name) {
    InterfacesInterface iface = _interfaces.get(name);
    if (iface == null) {
      iface = new InterfacesInterface(name);
      _interfaces.put(name, iface);
    }
    return iface;
  }

  public boolean hasVrf(String vrfName) {
    return _interfaces.values().stream()
        .anyMatch(iface -> iface.getName().equals(vrfName) && isVrf(iface));
  }

  @Nonnull
  public Set<String> getAutoIfaces() {
    return _autoIfaces;
  }

  @Nonnull
  public Map<String, InterfacesInterface> getInterfaces() {
    return _interfaces;
  }
}
