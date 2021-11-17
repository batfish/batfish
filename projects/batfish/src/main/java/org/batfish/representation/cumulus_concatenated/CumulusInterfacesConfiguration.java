package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.representation.cumulus_concatenated.InterfaceConverter.isVrf;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Model of a Cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class CumulusInterfacesConfiguration implements Serializable {
  @Nonnull private final Set<String> _autoIfaces;
  @Nonnull private final Map<String, InterfacesInterface> _interfaces;
  private List<String> _interfaceInitOrder;

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

  @Nonnull
  public Collection<String> getInterfaceInitOrder() {
    return firstNonNull(
        _interfaceInitOrder,
        // for ease of testing
        Collections.unmodifiableSet(_interfaces.keySet()));
  }

  public void setInterfaceInitOrder(Iterable<String> interfaceInitOrder) {
    _interfaceInitOrder = ImmutableList.copyOf(interfaceInitOrder);
  }
}
