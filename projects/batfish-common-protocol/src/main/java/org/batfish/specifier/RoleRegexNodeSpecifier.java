package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;

/**
 * A {@link NodeSpecifier} that specifies the set of nodes with a role with the input dimension and
 * a name matching the input regex.
 */
@ParametersAreNonnullByDefault
public final class RoleRegexNodeSpecifier implements NodeSpecifier {
  @Nonnull private final Pattern _rolePattern;
  @Nullable private final String _roleDimension;

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
    Set<NodeRole> roles =
        dimension.isPresent()
            ? dimension
                .get()
                .getRoles()
                .stream()
                .filter(role -> _rolePattern.matcher(role.getName()).matches())
                .collect(ImmutableSet.toImmutableSet())
            : ImmutableSet.of();

    return ctxt.getConfigs()
        .keySet()
        .stream()
        .filter(node -> roles.stream().anyMatch(role -> role.matches(node)))
        .collect(ImmutableSet.toImmutableSet());
  }
}
