package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.batfish.role.RoleMapping;

public class RoleMappingBean {

  public String name;
  public String regex;
  public Map<String, List<Integer>> roleDimensionGroups;
  public Map<String, Map<String, String>> canonicalRoleNames;

  @JsonCreator
  private RoleMappingBean() {}

  /** Instantiate this bean from {@code roleMapping}. */
  public RoleMappingBean(RoleMapping roleMapping) {
    name = roleMapping.getName().orElse(null);
    regex = roleMapping.getRegex();
    roleDimensionGroups = roleMapping.getRoleDimensionsGroups();
    canonicalRoleNames = roleMapping.getCanonicalRoleNames();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof org.batfish.coordinator.resources.RoleMappingBean)) {
      return false;
    }
    return Objects.equals(name, ((RoleMappingBean) o).name)
        && Objects.equals(regex, ((RoleMappingBean) o).regex)
        && Objects.equals(roleDimensionGroups, ((RoleMappingBean) o).roleDimensionGroups)
        && Objects.equals(canonicalRoleNames, ((RoleMappingBean) o).canonicalRoleNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, regex, roleDimensionGroups, canonicalRoleNames);
  }

  /** Gets a {@link RoleMapping} object from this bean. */
  public RoleMapping toRoleMapping() {
    return new RoleMapping(name, regex, roleDimensionGroups, canonicalRoleNames);
  }
}
