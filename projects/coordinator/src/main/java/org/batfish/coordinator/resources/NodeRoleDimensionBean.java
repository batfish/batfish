package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;

public class NodeRoleDimensionBean {

  public String name;
  public List<RoleDimensionMappingBean> roleDimensionMappings;
  public String snapshot;
  public NodeRoleDimension.Type type;

  @JsonCreator
  private NodeRoleDimensionBean() {}

  public NodeRoleDimensionBean(NodeRoleDimension nrDim, String snapshot) {
    this.name = nrDim.getName();
    this.roleDimensionMappings =
        nrDim.getRoleDimensionMappings().stream()
            .map(m -> new RoleDimensionMappingBean(m))
            .collect(Collectors.toList());
    this.snapshot = snapshot;
    this.type = nrDim.getType();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimensionBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleDimensionBean) o).name)
        && Objects.equals(roleDimensionMappings, ((NodeRoleDimensionBean) o).roleDimensionMappings)
        && Objects.equals(snapshot, ((NodeRoleDimensionBean) o).snapshot)
        && Objects.equals(type, ((NodeRoleDimensionBean) o).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roleDimensionMappings, snapshot, type);
  }

  /** Returns a {@link NodeRoleDimension} object corresponding to this bean */
  public NodeRoleDimension toNodeRoleDimension() {
    List<RoleDimensionMapping> rdMappings =
        roleDimensionMappings == null
            ? ImmutableList.of()
            : roleDimensionMappings.stream()
                .map(RoleDimensionMappingBean::toRoleDimensionMapping)
                .collect(ImmutableList.toImmutableList());
    return NodeRoleDimension.builder()
        .setName(name)
        .setRoleDimensionMappings(rdMappings)
        .setType(type)
        .build();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("name", name)
        .add("mappings", roleDimensionMappings)
        .add("snapshot", snapshot)
        .add("type", type)
        .toString();
  }
}
