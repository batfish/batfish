package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.role.NodeRole;

/**
 * A {@link NodeSpecifier} that specifies the set of nodes with a role with the input dimension and
 * a name matching the input regex.
 */
public class RoleRegexNodeSpecifier implements NodeSpecifier {
  private final Pattern _rolePattern;
  private final String _roleDimension;

  public RoleRegexNodeSpecifier(Pattern rolePattern, String roleDimension) {
    _rolePattern = rolePattern;
    _roleDimension = roleDimension;
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    Set<NodeRole> roles =
        ctxt.getNodeRolesByDimension(_roleDimension)
            .stream()
            .filter(role -> _rolePattern.matcher(role.getName()).matches())
            .collect(Collectors.toSet());

    return ctxt.getConfigs()
        .keySet()
        .stream()
        .filter(node -> roles.stream().anyMatch(role -> role.matches(node)))
        .collect(ImmutableSet.toImmutableSet());
  }
}
