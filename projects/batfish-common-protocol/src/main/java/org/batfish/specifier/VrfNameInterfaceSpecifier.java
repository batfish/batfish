package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * An {@link InterfaceSpecifier} for interfaces that belong to a VRF name. The name matching is
 * case-insensitive.
 */
public final class VrfNameInterfaceSpecifier implements InterfaceSpecifier {
  private final String _name;

  public VrfNameInterfaceSpecifier(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfNameInterfaceSpecifier)) {
      return false;
    }
    VrfNameInterfaceSpecifier that = (VrfNameInterfaceSpecifier) o;
    return Objects.equals(_name, that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  public Set<NodeInterfacePair> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return nodes.stream()
        .flatMap(n -> ctxt.getConfigs().get(n).getAllInterfaces().values().stream())
        .filter(i -> _name.equalsIgnoreCase(i.getVrfName()))
        .map(NodeInterfacePair::of)
        .collect(ImmutableSet.toImmutableSet());
  }
}
