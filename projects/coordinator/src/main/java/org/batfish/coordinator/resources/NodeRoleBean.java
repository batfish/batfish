package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.role.NodeRole;

public class NodeRoleBean {

  public String name;
  public Set<String> nodes;
  public String regex;

  @JsonCreator
  private NodeRoleBean() {}

  /**
   * Instantiate this bean from {@code role} and picking the subset of nodes in {@code fromNodes}
   * that match it.
   */
  public NodeRoleBean(NodeRole role, Set<String> fromNodes) {
    name = role.getName();
    regex = role.getRegex();
    nodes = fromNodes.stream().filter(role::matches).collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleBean) o).name)
        && Objects.equals(nodes, ((NodeRoleBean) o).nodes)
        && Objects.equals(regex, ((NodeRoleBean) o).regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, nodes, regex);
  }

  /**
   * Gets a {@link NodeRole} object from this bean.
   *
   * <p>Name and regex may be null in the bean. Error handling happens inside the NodeRole
   * constructor.
   */
  public NodeRole toNodeRole() {
    return new NodeRole(name, regex);
  }
}
