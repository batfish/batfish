package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRolesData;

/** A bean for node roles information. */
public class NodeRolesDataBean {

  public String defaultDimension;
  public Instant lastModifiedTime;
  public Set<NodeRoleDimensionBean> roleDimensions;

  @JsonCreator
  private NodeRolesDataBean() {}

  public NodeRolesDataBean(
      @Nonnull NodeRolesData nodeRolesData, String snapshot, Set<String> nodes) {
    defaultDimension = nodeRolesData.getDefaultDimension();
    lastModifiedTime = nodeRolesData.getLastModifiedTime();
    roleDimensions =
        nodeRolesData
            .getNodeRoleDimensions()
            .stream()
            .map(dim -> new NodeRoleDimensionBean(dim, snapshot, nodes))
            .collect(Collectors.toSet());
  }

  /**
   * creates a {@link NodeRolesDataBean} for the container using the set of nodes found in the
   * latest testrig
   */
  static NodeRolesDataBean create(@Nonnull String container) throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNodeRolesData(container);
    Optional<String> snapshot = Main.getWorkMgr().getLatestTestrig(container);
    Set<String> nodes =
        snapshot.isPresent()
            ? Main.getWorkMgr().getNodes(container, snapshot.get())
            : new TreeSet<>();
    return new NodeRolesDataBean(nodeRolesData, snapshot.orElse(null), nodes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesDataBean)) {
      return false;
    }
    // ignore lastModifiedTime for equality checking
    return Objects.equals(defaultDimension, ((NodeRolesDataBean) o).defaultDimension)
        && Objects.equals(roleDimensions, ((NodeRolesDataBean) o).roleDimensions);
  }

  @Override
  public int hashCode() {
    // ignore lastModifiedTime
    return Objects.hash(defaultDimension, roleDimensions);
  }
}
