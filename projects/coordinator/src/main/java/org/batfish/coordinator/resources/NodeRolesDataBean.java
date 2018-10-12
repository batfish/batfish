package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.role.NodeRolesData;

/** A bean for node roles information. */
public class NodeRolesDataBean {

  public String defaultDimension;
  public Set<NodeRoleDimensionBean> roleDimensions;

  @JsonCreator
  private NodeRolesDataBean() {}

  public NodeRolesDataBean(
      @Nonnull NodeRolesData nodeRolesData, @Nullable String snapshot, @Nonnull Set<String> nodes) {
    defaultDimension = nodeRolesData.getDefaultDimension();
    roleDimensions =
        nodeRolesData
            .getNodeRoleDimensions()
            .stream()
            .map(dim -> new NodeRoleDimensionBean(dim, snapshot, nodes))
            .collect(Collectors.toSet());
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

  public @Nonnull NodeRolesData toNodeRolesData() {
    return NodeRolesData.builder()
        .setDefaultDimension(defaultDimension)
        .setRoleDimensions(
            roleDimensions
                .stream()
                .map(NodeRoleDimensionBean::toNodeRoleDimension)
                .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural())))
        .build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("defaultDimension", defaultDimension)
        .add("roleDimensions", roleDimensions)
        .toString();
  }
}
