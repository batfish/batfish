package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.role.NodeRole;

/**
 * A {@link LocationSpecifier} specifying VRFs belonging to nodes with roles matching the input
 * dimension and regex.
 */
public class NodeRoleRegexVrfLocationSpecifier implements LocationSpecifier {
  private final String _roleDimension;

  private final Pattern _roleNamePattern;

  public NodeRoleRegexVrfLocationSpecifier(String roleDimension, Pattern roleNamePattern) {
    _roleDimension = roleDimension;
    _roleNamePattern = roleNamePattern;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    Set<NodeRole> matchingRoles =
        ctxt.getNodeRolesByDimension(_roleDimension)
            .stream()
            .filter(role -> _roleNamePattern.matcher(role.getName()).matches())
            .collect(ImmutableSet.toImmutableSet());
    return ctxt.getConfigs()
        .entrySet()
        .stream()
        .filter(entry -> matchingRoles.stream().anyMatch(role -> role.matches(entry.getKey())))
        .flatMap(
            entry ->
                entry
                    .getValue()
                    .getVrfs()
                    .keySet()
                    .stream()
                    .map(vrfName -> new VrfLocation(entry.getKey(), vrfName)))
        .collect(ImmutableSet.toImmutableSet());
  }
}
