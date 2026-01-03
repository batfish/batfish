package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;

/** Deprecated in favor of {@link NodeRolesData}. */
@ParametersAreNonnullByDefault
public final class NodeRoleDimension implements Comparable<NodeRoleDimension> {

  public static final class Builder {

    private String _name;

    private List<RoleDimensionMapping> _roleDimensionMappings;

    private Builder() {
      _roleDimensionMappings = ImmutableList.of();
    }

    public @Nonnull NodeRoleDimension build() {
      checkArgument(_name != null, "Name of node role dimension cannot be null");
      Names.checkName(_name, "role dimension", Names.Type.REFERENCE_OBJECT);
      return new NodeRoleDimension(_name, _roleDimensionMappings);
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setRoleDimensionMappings(List<RoleDimensionMapping> mappings) {
      _roleDimensionMappings = ImmutableList.copyOf(mappings);
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static @Nonnull Builder builder(String name) {
    return new Builder().setName(name);
  }

  public static final String AUTO_DIMENSION_PREFIX = "auto";

  public static final String AUTO_DIMENSION_PRIMARY = "auto0";
  private static final String PROP_NAME = "name";
  private static final String PROP_ROLES = "roles";
  private static final String PROP_ROLE_REGEXES = "roleRegexes";
  private static final String PROP_ROLE_DIMENSION_MAPPINGS = "roleDimensionMappings";

  private final @Nonnull String _name;

  /**
   * a list of role dimension mappings used to identify roles from node names. each mapping contains
   * a regex and a list of groups, where the groups identify the relevant parts of a node name for
   * this dimension, along with a mapping to produce canonical role names. it also contains the set
   * of node names for which to produce role names. there are multiple role dimension mappings to
   * handle node names that have different formats.
   */
  private final @Nonnull List<RoleDimensionMapping> _roleDimensionMappings;

  private NodeRoleDimension(String name, List<RoleDimensionMapping> mappings) {
    _name = name;
    _roleDimensionMappings = mappings;
  }

  /**
   * Create a node role dimension.
   *
   * @param name the name of the dimension
   * @param roles for backward-compatibility with an older format for node role dimensions, a list
   *     of node roles is accepted and each is converted into a role dimension mapping (see below)
   * @param roleRegexes for backward-compatibility with an older format for node role dimensions, a
   *     list of role regexes is accepted **but has no effect on the produced node role dimension**
   * @param mappings a list of role dimension mappings, which specify how to identify role names for
   *     this dimension from node names
   * @return the new node role dimension
   */
  @JsonCreator
  private static @Nonnull NodeRoleDimension create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_ROLES) @Nullable List<NodeRole> roles,
      @JsonProperty(PROP_ROLE_REGEXES) @Nullable List<String> ignoredRoleRegexes,
      @JsonProperty(PROP_ROLE_DIMENSION_MAPPINGS) @Nullable List<RoleDimensionMapping> mappings) {
    checkArgument(name != null, "Name of node role cannot be null");
    List<RoleDimensionMapping> rdMaps =
        new LinkedList<>(firstNonNull(mappings, ImmutableList.of()));
    List<NodeRole> nodeRoles = firstNonNull(roles, ImmutableList.of());
    rdMaps.addAll(nodeRoles.stream().map(RoleDimensionMapping::new).collect(Collectors.toList()));
    return new NodeRoleDimension(name, ImmutableList.copyOf(rdMaps));
  }

  private static final Comparator<NodeRoleDimension> COMPARATOR =
      comparing(NodeRoleDimension::getName)
          .thenComparing(
              NodeRoleDimension::getRoleDimensionMappings,
              nullsFirst(Comparators.lexicographical(RoleDimensionMapping::compareTo)));

  @Override
  public int compareTo(NodeRoleDimension o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeRoleDimension)) {
      return false;
    }
    NodeRoleDimension other = (NodeRoleDimension) o;
    return _name.equals(other._name) && _roleDimensionMappings.equals(other._roleDimensionMappings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _roleDimensionMappings);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE_DIMENSION_MAPPINGS)
  public @Nonnull List<RoleDimensionMapping> getRoleDimensionMappings() {
    return _roleDimensionMappings;
  }

  /**
   * Return the set of all roles played by at least one node in the given set of node names.
   *
   * @param nodeNames the set of node names
   * @return the set of role names
   */
  public Set<String> roleNamesFor(Set<String> nodeNames) {
    return createRoleNodesMap(nodeNames).keySet();
  }

  /**
   * Create a map from each node name to its set of roles
   *
   * @param nodeNames The universe of nodes that we need to classify
   * @return The created map
   */
  public SortedMap<String, String> createNodeRolesMap(Set<String> nodeNames) {
    // convert to a set that supports element removal
    TreeSet<String> nodes = new TreeSet<>(nodeNames);
    SortedMap<String, String> nodeRolesMap = new TreeMap<>();
    for (RoleDimensionMapping rdmap : _roleDimensionMappings) {
      SortedMap<String, String> currMap = rdmap.createNodeRolesMap(nodes);
      nodes.removeAll(currMap.keySet());
      nodeRolesMap.putAll(currMap);
    }
    return nodeRolesMap;
  }

  /**
   * Create a map from each role name to the set of nodes that play that role
   *
   * @param nodeNames The universe of nodes that we need to classify
   * @return The created map
   */
  public SortedMap<String, SortedSet<String>> createRoleNodesMap(Set<String> nodeNames) {
    SortedMap<String, SortedSet<String>> roleNodesMap = new TreeMap<>();
    SortedMap<String, String> nodeRolesMap = createNodeRolesMap(nodeNames);
    for (Map.Entry<String, String> entry : nodeRolesMap.entrySet()) {
      String nodeName = entry.getKey();
      String roleName = entry.getValue();
      SortedSet<String> roleNodes = roleNodesMap.computeIfAbsent(roleName, k -> new TreeSet<>());
      roleNodes.add(nodeName);
    }
    return roleNodesMap;
  }

  public boolean nodeHasRoleName(String nodeName, String roleName) {
    return roleNamesFor(ImmutableSortedSet.of(nodeName)).contains(roleName);
  }

  /**
   * Convert this node role dimension into an equivalent list of role mappings.
   *
   * @return the role mappings
   */
  public List<RoleMapping> toRoleMappings() {
    List<RoleMapping> mappings = new LinkedList<>();
    for (RoleDimensionMapping rdMap : _roleDimensionMappings) {
      mappings.add(
          new RoleMapping(
              null,
              rdMap.getRegex(),
              ImmutableMap.of(_name, rdMap.getGroups()),
              ImmutableMap.of(_name, rdMap.getCanonicalRoleNames())));
    }
    return copyOf(mappings);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_ROLE_DIMENSION_MAPPINGS, _roleDimensionMappings)
        .toString();
  }
}
