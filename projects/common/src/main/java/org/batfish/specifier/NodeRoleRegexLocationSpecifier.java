package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;
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
    if (dimension.isPresent()) {
      NodeRoleDimension nrdim = dimension.get();
      Map<String, SortedSet<String>> roleNodesMap =
          nrdim.createRoleNodesMap(ctxt.getConfigs().keySet());
      return roleNodesMap.keySet().stream()
          .filter(roleName -> _rolePattern.matcher(roleName).matches())
          .flatMap(roleName -> roleNodesMap.get(roleName).stream())
          .flatMap(nodeName -> getNodeLocations(ctxt.getConfigs().get(nodeName)))
          .collect(ImmutableSet.toImmutableSet());
    } else {
      return ImmutableSet.of();
    }
  }
}
