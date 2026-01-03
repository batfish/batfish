package org.batfish.specifier;

import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.NodeRoleDimension;

/** A {@link NodeSpecifier} that specifies the set of nodes with a role and dimension names. */
@ParametersAreNonnullByDefault
public final class RoleNameNodeSpecifier implements NodeSpecifier {
  private final @Nonnull String _roleName;
  private final @Nonnull String _roleDimension;

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
    NodeRoleDimension nodeRoleDimension =
        ctxt.getNodeRoleDimension(_roleDimension)
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Role dimension '" + _roleDimension + "' not found"));
    SortedMap<String, SortedSet<String>> roleNodesMap =
        nodeRoleDimension.createRoleNodesMap(ctxt.getConfigs().keySet());
    return roleNodesMap.entrySet().stream()
        .filter(e -> e.getKey().equalsIgnoreCase(_roleName))
        .findAny()
        .map(Entry::getValue)
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "Role name '"
                        + _roleName
                        + "' not found in dimension '"
                        + _roleDimension
                        + "'"));
  }
}
