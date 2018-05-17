package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.role.NodeRole;

public class RoleRegexNodeSpecifier implements NodeSpecifier {
  private final Pattern _rolePattern;
  private final String _roleDimension;

  public RoleRegexNodeSpecifier(Pattern rolePattern, String roleDimension) {
    _rolePattern = rolePattern;
    _roleDimension = roleDimension;
  }

  @Override
  public Set<String> resolve(SpecifierContext specifierContext) {
    Set<NodeRole> roles =
        specifierContext
            .getNodeRolesByDimension(_roleDimension)
            .stream()
            .filter(role -> _rolePattern.matcher(role.getName()).matches())
            .collect(Collectors.toSet());

    return specifierContext
        .getConfigs()
        .keySet()
        .stream()
        .filter(node -> roles.stream().anyMatch(role -> role.matches(node)))
        .collect(ImmutableSet.toImmutableSet());
  }
}
