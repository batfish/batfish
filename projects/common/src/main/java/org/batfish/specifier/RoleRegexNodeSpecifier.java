package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.NodeRoleDimension;

/**
 * A {@link NodeSpecifier} that specifies the set of nodes with a role with the input dimension and
 * a name matching the input regex.
 */
@ParametersAreNonnullByDefault
public final class RoleRegexNodeSpecifier implements NodeSpecifier {
  private final @Nonnull Pattern _rolePattern;
  private final @Nullable String _roleDimension;

  public RoleRegexNodeSpecifier(Pattern rolePattern, @Nullable String roleDimension) {
    _rolePattern = rolePattern;
    _roleDimension = roleDimension;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleRegexNodeSpecifier)) {
      return false;
    }
    // .pattern() based equality ignores options like Pattern.CASE_INSENSITIVE
    return Objects.equals(
            _rolePattern.pattern(), ((RoleRegexNodeSpecifier) o)._rolePattern.pattern())
        && Objects.equals(_roleDimension, ((RoleRegexNodeSpecifier) o)._roleDimension);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_rolePattern, _roleDimension);
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    Optional<NodeRoleDimension> dimension = ctxt.getNodeRoleDimension(_roleDimension);
    if (dimension.isPresent()) {
      NodeRoleDimension nrDimension = dimension.get();
      Map<String, SortedSet<String>> roleNodesMap =
          nrDimension.createRoleNodesMap(ctxt.getConfigs().keySet());

      return roleNodesMap.keySet().stream()
          .filter(roleName -> _rolePattern.matcher(roleName).matches())
          .flatMap(roleName -> roleNodesMap.get(roleName).stream())
          .collect(ImmutableSet.toImmutableSet());
    } else {
      return ImmutableSet.of();
    }
  }
}
