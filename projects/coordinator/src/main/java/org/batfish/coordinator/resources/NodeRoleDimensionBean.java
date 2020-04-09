package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;

public class NodeRoleDimensionBean {

  public String name;
  // list of NodeRoles kept for backward-compatibility with an older node roles format
  public List<NodeRoleBean> roles;
  public List<RoleDimensionMappingBean> roleDimensionMappings;
  public String snapshot;

  @JsonCreator
  private NodeRoleDimensionBean() {
    // in case one or both of these is not provided, initialize them so they're non-null
    roles = ImmutableList.of();
    roleDimensionMappings = ImmutableList.of();
  }

  public NodeRoleDimensionBean(NodeRoleDimension nrDim, String snapshot) {
    name = nrDim.getName();
    roles = ImmutableList.of();
    roleDimensionMappings =
        nrDim.getRoleDimensionMappings().stream()
            .map(RoleDimensionMappingBean::new)
            .collect(Collectors.toList());
    this.snapshot = snapshot;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimensionBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleDimensionBean) o).name)
        && Objects.equals(roles, ((NodeRoleDimensionBean) o).roles)
        && Objects.equals(roleDimensionMappings, ((NodeRoleDimensionBean) o).roleDimensionMappings)
        && Objects.equals(snapshot, ((NodeRoleDimensionBean) o).snapshot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roles, roleDimensionMappings, snapshot);
  }

  /** Returns a {@link NodeRoleDimension} object corresponding to this bean */
  public NodeRoleDimension toNodeRoleDimension() {
    List<RoleDimensionMapping> rdMappings = new LinkedList<>();
    rdMappings.addAll(
        roleDimensionMappings.stream()
            .map(RoleDimensionMappingBean::toRoleDimensionMapping)
            .collect(ImmutableList.toImmutableList()));
    rdMappings.addAll(
        roles.stream()
            .map(NodeRoleBean::toNodeRole)
            .map(RoleDimensionMapping::new)
            .collect(ImmutableList.toImmutableList()));
    return NodeRoleDimension.builder().setName(name).setRoleDimensionMappings(rdMappings).build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("name", name)
        .add("roles", roles)
        .add("mappings", roleDimensionMappings)
        .add("snapshot", snapshot)
        .toString();
  }
}
