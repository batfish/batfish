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

  public @Nullable String defaultDimension;
  public List<RoleMappingBean> roleMappings;
  public @Nullable List<String> roleDimensionOrder;
  public NodeRolesData.Type type;
  public @Nullable String snapshot;

  @JsonCreator
  private NodeRolesDataBean() {}

  public NodeRolesDataBean(@Nonnull NodeRolesData nodeRolesData, @Nullable String snapshot) {
    defaultDimension = nodeRolesData.getDefaultDimension();
    roleMappings =
        nodeRolesData.getRoleMappings().stream()
            .map(rMap -> new RoleMappingBean(rMap))
            .collect(Collectors.toList());
    roleDimensionOrder = nodeRolesData.getRoleDimensionOrder().orElse(null);
    type = nodeRolesData.getType();
    this.snapshot = snapshot;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesDataBean)) {
      return false;
    }
    // ignore lastModifiedTime for equality checking
    return Objects.equals(defaultDimension, ((NodeRolesDataBean) o).defaultDimension)
        && Objects.equals(roleMappings, ((NodeRolesDataBean) o).roleMappings)
        && Objects.equals(roleDimensionOrder, ((NodeRolesDataBean) o).roleDimensionOrder)
        && Objects.equals(type, ((NodeRolesDataBean) o).type)
        && Objects.equals(snapshot, ((NodeRolesDataBean) o).snapshot);
  }

  @Override
  public int hashCode() {
    // ignore lastModifiedTime
    return Objects.hash(
        defaultDimension, roleMappings, roleDimensionOrder, type.ordinal(), snapshot);
  }

  public @Nonnull NodeRolesData toNodeRolesData() {
    return NodeRolesData.builder()
        .setDefaultDimension(defaultDimension)
        .setRoleMappings(
            roleMappings.stream()
                .map(RoleMappingBean::toRoleMapping)
                .collect(ImmutableList.toImmutableList()))
        .setRoleDimensionOrder(roleDimensionOrder)
        .setType(type)
        .build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("defaultDimension", defaultDimension)
        .add("roleMappings", roleMappings)
        .add("roleDimensionOrder", roleDimensionOrder)
        .add("type", type)
        .add("snapshot", snapshot)
        .toString();
  }
}
