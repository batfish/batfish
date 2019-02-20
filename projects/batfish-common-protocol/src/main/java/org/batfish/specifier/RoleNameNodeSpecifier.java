package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.NodeRole;

/** A {@link NodeSpecifier} that specifies the set of nodes with a role and dimension names. */
@ParametersAreNonnullByDefault
public final class RoleNameNodeSpecifier implements NodeSpecifier {
  @Nonnull private final String _roleName;
  @Nonnull private final String _roleDimension;

  public RoleNameNodeSpecifier(String roleName, String roleDimension) {
    _roleName = roleName;
    _roleDimension = roleDimension;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleNameNodeSpecifier)) {
      return false;
    }
    // .pattern() based equality ignores options like Pattern.CASE_INSENSITIVE
    return Objects.equals(_roleName, ((RoleNameNodeSpecifier) o)._roleName)
        && Objects.equals(_roleDimension, ((RoleNameNodeSpecifier) o)._roleDimension);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_roleName, _roleDimension);
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    NodeRole nodeRole =
        ctxt.getNodeRoleDimension(_roleDimension)
            .orElseThrow(
                () ->
                    new NoSuchElementException("Role dimension '" + _roleDimension + "' not found"))
            .getRoles().stream()
            .filter(r -> r.getName().equalsIgnoreCase(_roleName))
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Role name '"
                            + _roleName
                            + "' not found in dimension '"
                            + _roleDimension
                            + "'"));

    return ctxt.getConfigs().keySet().stream()
        .filter(node -> nodeRole.matches(node))
        .collect(ImmutableSet.toImmutableSet());
  }
}
