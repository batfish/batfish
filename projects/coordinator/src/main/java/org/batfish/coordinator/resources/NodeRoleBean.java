package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.role.NodeRole;

public class NodeRoleBean {

  public String name;
  public Set<String> nodes;
  public String regex;

  /** Used by Jackson */
  private NodeRoleBean() {}

  /**
   * Instantiate this bean from {@code role} and picking the subset of nodes in {@code fromNodes}
   * that match it.
   */
  public NodeRoleBean(NodeRole role, Set<String> fromNodes) {
    name = role.getName();
    regex = role.getRegex();
    nodes =
        fromNodes
            .stream()
            .filter(node -> role.matches(node))
            .collect(ImmutableSet.toImmutableSet());
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
}
