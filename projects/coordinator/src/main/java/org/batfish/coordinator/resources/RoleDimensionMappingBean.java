package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.batfish.role.RoleDimensionMapping;

public class RoleDimensionMappingBean {

  public String regex;
  public List<Integer> groups;
  public Map<String, String> canonicalRoleNames;

  @JsonCreator
  private RoleDimensionMappingBean() {}

  /** Instantiate this bean from {@code rdMapping}. */
  public RoleDimensionMappingBean(RoleDimensionMapping rdMapping) {
    regex = rdMapping.getRegex();
    groups = rdMapping.getGroups();
    canonicalRoleNames = rdMapping.getCanonicalRoleNames();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof org.batfish.coordinator.resources.RoleDimensionMappingBean)) {
      return false;
    }
    return Objects.equals(
            regex, ((org.batfish.coordinator.resources.RoleDimensionMappingBean) o).regex)
        && Objects.equals(
            groups, ((org.batfish.coordinator.resources.RoleDimensionMappingBean) o).groups)
        && Objects.equals(canonicalRoleNames, ((RoleDimensionMappingBean) o).canonicalRoleNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(regex, groups, canonicalRoleNames);
  }

  /** Gets a {@link RoleDimensionMapping} object from this bean. */
  public RoleDimensionMapping toRoleDimensionMapping() {
    return new RoleDimensionMapping(regex, groups, canonicalRoleNames);
  }
}
