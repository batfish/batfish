package org.batfish.coordinator.resources;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.role.NodeRoleDimension;

public class NodeRoleDimensionBean {

  public String name;
  public Set<NodeRoleBean> roles;
  public NodeRoleDimension.Type type;

  /** Used by Jackson */
  private NodeRoleDimensionBean() {}

  public NodeRoleDimensionBean(NodeRoleDimension nrDim, Set<String> fromNodes) {
    name = nrDim.getName();
    roles =
        nrDim
            .getRoles()
            .stream()
            .map(role -> new NodeRoleBean(role, fromNodes))
            .collect(Collectors.toSet());
    type = nrDim.getType();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimensionBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleDimensionBean) o).name)
        && Objects.equals(roles, ((NodeRoleDimensionBean) o).roles)
        && Objects.equals(type, ((NodeRoleDimensionBean) o).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roles, type);
  }
}
