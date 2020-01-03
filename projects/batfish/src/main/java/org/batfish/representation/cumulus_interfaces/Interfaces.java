package org.batfish.representation.cumulus_interfaces;

import static org.batfish.representation.cumulus_interfaces.Converter.isInterface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.cumulus.Loopback;

/** Model of a Cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class Interfaces implements Serializable {
  @Nonnull private final Set<String> _autoIfaces;
  @Nonnull private final Map<String, Interface> _interfaces;
  @Nonnull private final Loopback _loopback;

  public Interfaces() {
    _autoIfaces = new HashSet<>();
    _interfaces = new HashMap<>();
    _loopback = new Loopback();
  }

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

  public boolean hasVrf(String vrfName) {
    return _interfaces.values().stream()
        .anyMatch(iface -> iface.getName().equals(vrfName) && isInterface(iface));
  }

  @Nonnull
  public Loopback getLoopback() {
    return _loopback;
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
