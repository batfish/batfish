package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;

public class NodeRoleDimensionBean {

  public String name;
  public List<RoleDimensionMappingBean> mappings;
  public String snapshot;
  public NodeRoleDimension.Type type;
  public Map<String, String> nodeRolesMap;

  @JsonCreator
  private NodeRoleDimensionBean() {}

  public NodeRoleDimensionBean(NodeRoleDimension nrDim, String snapshot, Set<String> fromNodes) {
    this.name = nrDim.getName();
    this.mappings =
        nrDim
            .getRoleDimensionMappings()
            .stream()
            .map(m -> new RoleDimensionMappingBean(m, fromNodes))
            .collect(Collectors.toList());
    this.snapshot = snapshot;
    this.type = nrDim.getType();
    this.nodeRolesMap = nrDim.createNodeRolesMap(fromNodes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimensionBean)) {
      return false;
    }
    return Objects.equals(name, ((NodeRoleDimensionBean) o).name)
        && Objects.equals(mappings, ((NodeRoleDimensionBean) o).mappings)
        && Objects.equals(snapshot, ((NodeRoleDimensionBean) o).snapshot)
        && Objects.equals(type, ((NodeRoleDimensionBean) o).type)
        && Objects.equals(nodeRolesMap, ((NodeRoleDimensionBean) o).nodeRolesMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, mappings, snapshot, type, nodeRolesMap);
  }

  /** Returns a {@link NodeRoleDimension} object corresponding to this bean */
  public NodeRoleDimension toNodeRoleDimension() {
    List<RoleDimensionMapping> rdMappings =
        mappings == null
            ? ImmutableList.of()
            : mappings
                .stream()
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
        .add("nodeRolesMap", nodeRolesMap)
        .add("mappings", mappings)
        .add("snapshot", snapshot)
        .add("type", type)
        .toString();
  }
}
