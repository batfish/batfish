package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.role.NodeRolesData;

/** A bean for node roles information. */
public class NodeRolesDataBean {

  public String defaultDimension;
  public Instant lastModifiedTime;
  public String latestSnapshot;
  public Set<NodeRoleDimensionBean> roleDimensions;

  @JsonCreator
  private NodeRolesDataBean() {}

  public NodeRolesDataBean(
      @Nonnull NodeRolesData nrData, Optional<String> snapshot, @Nonnull Set<String> fromNodes) {
    defaultDimension = nrData.getDefaultDimension();
    lastModifiedTime = nrData.getLastModifiedTime();
    roleDimensions =
        nrData
            .getNodeRoleDimensions()
            .stream()
            .map(dim -> new NodeRoleDimensionBean(dim, fromNodes))
            .collect(Collectors.toSet());
    latestSnapshot = snapshot.isPresent() ? snapshot.get() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesDataBean)) {
      return false;
    }
    // ignore lastModifiedTime for equality checking
    return Objects.equals(defaultDimension, ((NodeRolesDataBean) o).defaultDimension)
        && Objects.equals(latestSnapshot, ((NodeRolesDataBean) o).latestSnapshot)
        && Objects.equals(roleDimensions, ((NodeRolesDataBean) o).roleDimensions);
  }

  @Override
  public int hashCode() {
    // ignore lastModifiedTime
    return Objects.hash(defaultDimension, latestSnapshot, roleDimensions);
  }
}
