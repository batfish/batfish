package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;

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

  /**
   * Creates a {@link NodeRoleDimensionBean} from the given container and dimension. It first gets
   * the NodeRolesDataBean and finds the right dimension in it.
   *
   * @returns A NodeRoleDimensionBean object or null if the requested dimension is not found
   */
  static NodeRoleDimensionBean create(String container, String dimension) throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNodeRolesData(container);
    Optional<NodeRoleDimension> optDim = nodeRolesData.getNodeRoleDimension(dimension);
    if (!optDim.isPresent()) {
      return null;
    }
    Optional<String> snapshot = Main.getWorkMgr().getLatestTestrig(container);
    Set<String> nodes =
        snapshot.isPresent()
            ? Main.getWorkMgr().getNodes(container, snapshot.get())
            : new TreeSet<>();
    return new NodeRoleDimensionBean(optDim.get(), snapshot.orElse(null), nodes);
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
            ? null
            : roles
                .stream()
                .map(NodeRoleBean::toNodeRole)
                .collect(ImmutableSortedSet.toImmutableSortedSet(NodeRole::compareTo));
    return new NodeRoleDimension(name, nodeRoles, type, null);
  }
}
