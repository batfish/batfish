package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.role.NodeRolesData;

/** A bean for node roles information. */
public class NodeRolesDataBean {

  public String defaultDimension;
  public List<NodeRoleDimensionBean> roleDimensions;
  public List<String> roleDimensionOrder;

  @JsonCreator
  private NodeRolesDataBean() {}

  public NodeRolesDataBean(@Nonnull NodeRolesData nodeRolesData, @Nullable String snapshot) {
    defaultDimension = nodeRolesData.getDefaultDimension();
    roleDimensions =
        nodeRolesData.getNodeRoleDimensions().stream()
            .map(dim -> new NodeRoleDimensionBean(dim, snapshot))
            .collect(Collectors.toList());
    roleDimensionOrder = nodeRolesData.getRoleDimensionOrder().orElse(null);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesDataBean)) {
      return false;
    }
    // ignore lastModifiedTime for equality checking
    return Objects.equals(defaultDimension, ((NodeRolesDataBean) o).defaultDimension)
        && Objects.equals(roleDimensions, ((NodeRolesDataBean) o).roleDimensions)
        && Objects.equals(roleDimensionOrder, ((NodeRolesDataBean) o).roleDimensionOrder);
  }

  @Override
  public int hashCode() {
    // ignore lastModifiedTime
    return Objects.hash(defaultDimension, roleDimensions, roleDimensionOrder);
  }

  public @Nonnull NodeRolesData toNodeRolesData() {
    return NodeRolesData.builder()
        .setDefaultDimension(defaultDimension)
        .setRoleDimensions(
            roleDimensions.stream()
                .map(NodeRoleDimensionBean::toNodeRoleDimension)
                .collect(ImmutableList.toImmutableList()))
        .setRoleDimensionOrder(roleDimensionOrder)
        .build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("defaultDimension", defaultDimension)
        .add("roleDimensions", roleDimensions)
        .add("roleDimensionOrder", roleDimensionOrder)
        .toString();
  }
}
