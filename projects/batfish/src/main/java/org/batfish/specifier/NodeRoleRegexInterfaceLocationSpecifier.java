package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.role.NodeRole;

public class NodeRoleRegexInterfaceLocationSpecifier implements LocationSpecifier {
  private final String _roleDimension;

  private final Pattern _rolePattern;

  public NodeRoleRegexInterfaceLocationSpecifier(String roleDimension, Pattern rolePattern) {
    _roleDimension = roleDimension;
    _rolePattern = rolePattern;
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
