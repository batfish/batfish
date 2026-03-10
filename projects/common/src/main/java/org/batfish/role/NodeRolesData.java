package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;

/** Class that captures the node roles */
@ParametersAreNonnullByDefault
public class NodeRolesData {

  public static final class Builder {
    private @Nullable String _defaultDimension;
    private @Nullable List<String> _roleDimensionOrder;
    private @Nonnull List<RoleMapping> _roleMappings;
    private @Nonnull Type _type;

    private Builder() {
      _roleMappings = ImmutableList.of();
      _type = Type.CUSTOM;
    }

    public @Nonnull NodeRolesData build() {
      return new NodeRolesData(_defaultDimension, _roleMappings, _roleDimensionOrder, _type);
    }

    public @Nonnull Builder setDefaultDimension(@Nullable String defaultDimension) {
      _defaultDimension = defaultDimension;
      return this;
    }

    public @Nonnull Builder setRoleDimensionOrder(@Nullable List<String> roleDimensionOrder) {
      _roleDimensionOrder = roleDimensionOrder == null ? null : copyOf(roleDimensionOrder);
      return this;
    }

    /**
     * For convenience a list of node role dimensions can be provided and is converted into a list
     * of role mappings.
     *
     * @param roleDimensions - the role dimensions
     * @return - a builder for node roles data that consists of the new role mappings
     */
    public @Nonnull Builder setRoleDimensions(List<NodeRoleDimension> roleDimensions) {
      _roleMappings =
          roleDimensions.stream()
              .flatMap(d -> d.toRoleMappings().stream())
              .collect(ImmutableList.toImmutableList());
      return this;
    }

    public @Nonnull Builder setRoleMappings(List<RoleMapping> roleMappings) {
      _roleMappings = copyOf(roleMappings);
      return this;
    }

    public @Nonnull Builder setType(Type type) {
      _type = type;
      return this;
    }
  }

  public enum Type {
    AUTO,
    CUSTOM
  }

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_ROLE_MAPPINGS = "roleMappings";
  private static final String PROP_ROLE_DIMENSION_ORDER = "roleDimensionOrder";
  private static final String PROP_TYPE = "type";

  private @Nullable String _defaultDimension;

  private @Nonnull List<RoleMapping> _roleMappings;

  /* the list of role dimensions (or a subset of them), ordered for hierarchical
    visualization / exploration
  */
  private @Nullable List<String> _roleDimensionOrder;

  private @Nonnull Type _type;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private NodeRolesData(
      @Nullable String defaultDimension,
      List<RoleMapping> roleMappings,
      @Nullable List<String> roleDimensionOrder,
      Type type) {
    checkNotNull(roleMappings);
    if (defaultDimension != null) {
      Names.checkName(
          defaultDimension, "role dimension", org.batfish.datamodel.Names.Type.REFERENCE_OBJECT);
    }
    _defaultDimension = defaultDimension;
    _roleMappings = roleMappings;
    _roleDimensionOrder = roleDimensionOrder;
    _type = type;
  }

  @JsonCreator
  private static @Nonnull NodeRolesData create(
      @JsonProperty(PROP_DEFAULT_DIMENSION) @Nullable String defaultDimension,
      @JsonProperty(PROP_ROLE_MAPPINGS) @Nullable List<RoleMapping> roleMappings,
      @JsonProperty(PROP_ROLE_DIMENSION_ORDER) @Nullable List<String> roleDimensionOrder,
      @JsonProperty(PROP_TYPE) Type type) {
    return new NodeRolesData(
        defaultDimension,
        copyOf(firstNonNull(roleMappings, ImmutableList.of())),
        roleDimensionOrder,
        type);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeRolesData)) {
      return false;
    }
    return Objects.equals(_defaultDimension, ((NodeRolesData) o)._defaultDimension)
        && Objects.equals(_roleMappings, ((NodeRolesData) o)._roleMappings)
        && Objects.equals(_roleDimensionOrder, ((NodeRolesData) o)._roleDimensionOrder)
        && Objects.equals(_type, ((NodeRolesData) o)._type);
  }

  @JsonProperty(PROP_DEFAULT_DIMENSION)
  public @Nullable String getDefaultDimension() {
    return _defaultDimension;
  }

  /**
   * Get a {@link NodeRoleDimension} object for the specified dimension. If dimension is null,
   * returns {@link #nodeRoleDimensionForDefault()}.
   *
   * @param dimension The name of the dimension to fetch
   * @return An {@link Optional} with {@link NodeRoleDimension} object if we have node roles data
   *     for the given dimension name.
   */
  public @Nonnull Optional<NodeRoleDimension> nodeRoleDimensionFor(@Nullable String dimension) {
    if (dimension == null) {
      return nodeRoleDimensionForDefault();
    } else {
      SortedMap<String, NodeRoleDimension> nrDims = toNodeRoleDimensions();
      return Optional.ofNullable(nrDims.get(dimension));
    }
  }

  /**
   * Get some "reasonable" {@link NodeRoleDimension} object for analysis. Preference order: the
   * default dimension if set and exists, the auto-inferred primary dimension if it exists, the
   * dimension that is lexicographically first, and null if no dimensions exist.
   */
  private @Nonnull Optional<NodeRoleDimension> nodeRoleDimensionForDefault() {
    // check default
    if (getDefaultDimension() != null) {
      Optional<NodeRoleDimension> opt = nodeRoleDimensionFor(getDefaultDimension());
      if (opt.isPresent()) {
        return opt;
      }
    }
    // check auto primary
    Optional<NodeRoleDimension> optAuto =
        nodeRoleDimensionFor(NodeRoleDimension.AUTO_DIMENSION_PRIMARY);
    if (optAuto.isPresent()) {
      return optAuto;
    }
    // check first
    SortedMap<String, NodeRoleDimension> nrDims = toNodeRoleDimensions();
    if (!nrDims.isEmpty()) {
      return Optional.of(nrDims.get(nrDims.firstKey()));
    }

    return Optional.empty();
  }

  /**
   * Produce a per-role-dimension "view" of the node roles data
   *
   * @return a map from each dimension name to its node role data
   */
  public @Nonnull SortedMap<String, NodeRoleDimension> toNodeRoleDimensions() {
    Map<String, List<RoleDimensionMapping>> rdMaps = new TreeMap<>();
    for (RoleMapping rmap : _roleMappings) {
      String regex = rmap.getRegex();
      Map<String, List<Integer>> rdGroups = rmap.getRoleDimensionsGroups();
      Map<String, Map<String, String>> canonicalRoleNames = rmap.getCanonicalRoleNames();
      for (Map.Entry<String, List<Integer>> entry : rdGroups.entrySet()) {
        String dim = entry.getKey();
        List<Integer> groups = entry.getValue();
        RoleDimensionMapping rdmap =
            new RoleDimensionMapping(
                regex, groups, canonicalRoleNames.getOrDefault(dim, ImmutableMap.of()));
        List<RoleDimensionMapping> dimMaps = rdMaps.computeIfAbsent(dim, k -> new LinkedList<>());
        dimMaps.add(rdmap);
      }
    }
    // now build the NodeRoleDimensions, one per dimension name
    SortedMap<String, NodeRoleDimension> nodeRoleDimensions = new TreeMap<>();
    for (Map.Entry<String, List<RoleDimensionMapping>> entry : rdMaps.entrySet()) {
      String dim = entry.getKey();
      List<RoleDimensionMapping> rdmaps = entry.getValue();
      nodeRoleDimensions.put(
          dim, NodeRoleDimension.builder(dim).setRoleDimensionMappings(rdmaps).build());
    }
    return nodeRoleDimensions;
  }

  @JsonProperty(PROP_ROLE_MAPPINGS)
  public @Nonnull List<RoleMapping> getRoleMappings() {
    return _roleMappings;
  }

  @JsonProperty(PROP_ROLE_DIMENSION_ORDER)
  public Optional<List<String>> getRoleDimensionOrder() {
    return Optional.ofNullable(_roleDimensionOrder);
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_defaultDimension, _roleMappings, _roleDimensionOrder, _type);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DEFAULT_DIMENSION, _defaultDimension)
        .add(PROP_ROLE_MAPPINGS, _roleMappings)
        .add(PROP_ROLE_DIMENSION_ORDER, _roleDimensionOrder)
        .add(PROP_TYPE, _type)
        .toString();
  }
}
