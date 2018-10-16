package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;

public class NodeRoleDimensionBean {

  public String name;
  public Set<NodeRoleBean> roles;
  public String snapshot;
  public NodeRoleDimension.Type type;

  @JsonCreator
  private NodeRoleDimensionBean() {}

  public NodeRoleDimensionBean(NodeRoleDimension nrDim, String snapshot, Set<String> fromNodes) {
    this(
        nrDim.getName(),
        nrDim
            .getRoles()
            .stream()
            .map(role -> new NodeRoleBean(role, fromNodes))
            .collect(Collectors.toSet()),
        snapshot,
        nrDim.getType());
  }

  public NodeRoleDimensionBean(
      String name, Set<NodeRoleBean> roles, String snapshot, NodeRoleDimension.Type type) {
    this.name = name;
    this.roles = roles;
    this.snapshot = snapshot;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimensionBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleDimensionBean) o).name)
        && Objects.equals(roles, ((NodeRoleDimensionBean) o).roles)
        && Objects.equals(snapshot, ((NodeRoleDimensionBean) o).snapshot)
        && Objects.equals(type, ((NodeRoleDimensionBean) o).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roles, snapshot, type);
  }

  /** Returns a {@link NodeRoleDimension} object corresponding to this bean */
  public NodeRoleDimension toNodeRoleDimension() {
    SortedSet<NodeRole> nodeRoles =
        roles == null
            ? ImmutableSortedSet.of()
            : roles
                .stream()
                .map(NodeRoleBean::toNodeRole)
                .collect(ImmutableSortedSet.toImmutableSortedSet(NodeRole::compareTo));
    return NodeRoleDimension.builder().setName(name).setRoles(nodeRoles).setType(type).build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("name", name)
        .add("roles", roles)
        .add("snapshot", snapshot)
        .add("type", type)
        .toString();
  }
}
