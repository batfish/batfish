package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;

/** An abstract {@link LocationSpecifier} specifying locations by regex on node role names. */
public abstract class NodeRoleRegexLocationSpecifier implements LocationSpecifier {
  private final String _roleDimension;

  private final Pattern _rolePattern;

  public NodeRoleRegexLocationSpecifier(String roleDimension, Pattern rolePattern) {
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
    NodeRoleRegexLocationSpecifier that = (NodeRoleRegexLocationSpecifier) o;
    return Objects.equals(_roleDimension, that._roleDimension)
        && Objects.equals(_rolePattern.pattern(), that._rolePattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_roleDimension, _rolePattern);
  }

  abstract Stream<Location> getNodeLocations(Configuration node);

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    Optional<NodeRoleDimension> dimension = ctxt.getNodeRoleDimension(_roleDimension);
    Set<NodeRole> matchingRoles =
        dimension.isPresent()
            ? dimension
                .get()
                .getRoles()
                .stream()
                .filter(role -> _rolePattern.matcher(role.getName()).matches())
                .collect(ImmutableSet.toImmutableSet())
            : ImmutableSet.of();

    return ctxt.getConfigs()
        .values()
        .stream()
        .filter(node -> matchingRoles.stream().anyMatch(role -> role.matches(node.getHostname())))
        .flatMap(this::getNodeLocations)
        .collect(ImmutableSet.toImmutableSet());
  }
}
