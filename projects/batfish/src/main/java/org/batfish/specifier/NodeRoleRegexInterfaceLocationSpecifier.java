package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.role.NodeRole;

/**
 * A {@link LocationSpecifier} specifying interfaces belonging to nodes with roles matching the
 * input dimension and regex.
 */
public class NodeRoleRegexInterfaceLocationSpecifier implements LocationSpecifier {
  private final String _roleDimension;

  private final Pattern _rolePattern;

  public NodeRoleRegexInterfaceLocationSpecifier(String roleDimension, Pattern rolePattern) {
    _roleDimension = roleDimension;
    _rolePattern = rolePattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeRoleRegexInterfaceLocationSpecifier that = (NodeRoleRegexInterfaceLocationSpecifier) o;
    return Objects.equals(_roleDimension, that._roleDimension)
        && Objects.equals(_rolePattern.pattern(), that._rolePattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_roleDimension, _rolePattern);
  }

  protected Location makeLocation(String node, String iface) {
    return new InterfaceLocation(node, iface);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    Set<NodeRole> matchingRoles =
        ctxt.getNodeRolesByDimension(_roleDimension)
            .stream()
            .filter(role -> _rolePattern.matcher(role.getName()).matches())
            .collect(ImmutableSet.toImmutableSet());
    return ctxt.getConfigs()
        .entrySet()
        .stream()
        .filter(entry -> matchingRoles.stream().anyMatch(role -> role.matches(entry.getKey())))
        .flatMap(
            entry ->
                entry
                    .getValue()
                    .getInterfaces()
                    .values()
                    .stream()
                    .map(iface -> makeLocation(iface.getOwner().getHostname(), iface.getName())))
        .collect(ImmutableSet.toImmutableSet());
  }
}
