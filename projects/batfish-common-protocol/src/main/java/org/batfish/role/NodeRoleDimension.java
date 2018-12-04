package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.CommonUtil;

@ParametersAreNonnullByDefault
public final class NodeRoleDimension implements Comparable<NodeRoleDimension> {

  public static final class Builder {

    private String _name;

    private List<String> _roleRegexes;

    private SortedSet<NodeRole> _roles;

    private Type _type;

    private Builder() {
      _roleRegexes = ImmutableList.of();
      _roles = ImmutableSortedSet.of();
      _type = Type.CUSTOM;
    }

    public @Nonnull NodeRoleDimension build() {
      checkArgument(_name != null, "Name of node role cannot be null");
      checkArgument(
          _type != Type.AUTO || _name.startsWith(AUTO_DIMENSION_PREFIX),
          "Name for a AUTO role dimension must begin with: %s",
          AUTO_DIMENSION_PREFIX);
      return new NodeRoleDimension(_name, _roles, firstNonNull(_type, Type.CUSTOM), _roleRegexes);
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setRoleRegexes(List<String> roleRegexes) {
      _roleRegexes = ImmutableList.copyOf(roleRegexes);
      return this;
    }

    public @Nonnull Builder setRoles(Set<NodeRole> roles) {
      _roles = ImmutableSortedSet.copyOf(roles);
      return this;
    }

    public @Nonnull Builder setType(Type type) {
      _type = type;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public enum Type {
    AUTO,
    CUSTOM
  }

  public static final String AUTO_DIMENSION_PREFIX = "auto";

  public static final String AUTO_DIMENSION_PRIMARY = "auto0";

  private static final String PROP_NAME = "name";

  private static final String PROP_ROLES = "roles";

  private static final String PROP_ROLE_REGEXES = "roleRegexes";

  private static final String PROP_TYPE = "type";

  @Nonnull private final String _name;

  /**
   * a list of regexes used to identify roles from node names. each regex in regexes has at least
   * one group in it that locates the role name within a node name. there are multiple regexes to
   * handle node names that have different formats, and to allow a node to have multple roles. this
   * value is usually populated by auto role inference and may be empty for custom role dimensions.
   */
  @Nonnull private final List<String> _roleRegexes;

  @Nonnull private final SortedSet<NodeRole> _roles;

  @Nonnull private final Type _type;

  private NodeRoleDimension(
      String name, SortedSet<NodeRole> roles, Type type, List<String> roleRegexes) {
    _name = name;
    _roles = roles;
    _type = type;
    _roleRegexes = roleRegexes;
  }

  @JsonCreator
  private static @Nonnull NodeRoleDimension create(
      @Nullable @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_ROLES) SortedSet<NodeRole> roles,
      @Nullable @JsonProperty(PROP_TYPE) Type type,
      @Nullable @JsonProperty(PROP_ROLE_REGEXES) List<String> roleRegexes) {
    checkArgument(name != null, "Name of node role cannot be null");
    checkArgument(
        type != Type.AUTO || name.startsWith(AUTO_DIMENSION_PREFIX),
        "Name for a AUTO role dimension must begin with: %s",
        AUTO_DIMENSION_PREFIX);
    return new NodeRoleDimension(
        name,
        firstNonNull(roles, ImmutableSortedSet.of()),
        firstNonNull(type, Type.CUSTOM),
        firstNonNull(roleRegexes, ImmutableList.of()));
  }

  private static final Comparator<NodeRoleDimension> COMPARATOR =
      comparing(NodeRoleDimension::getName)
          .thenComparing(NodeRoleDimension::getRoles, nullsFirst(CommonUtil::compareCollection))
          .thenComparing(NodeRoleDimension::getType)
          .thenComparing(
              NodeRoleDimension::getRoleRegexes, nullsFirst(CommonUtil::compareCollection));

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
    return _name.equals(other._name)
        && _roleRegexes.equals(other._roleRegexes)
        && _roles.equals(other._roles)
        && _type == other._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _roleRegexes, _roles, _type.ordinal());
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE_REGEXES)
  public @Nonnull List<String> getRoleRegexes() {
    return _roleRegexes;
  }

  @JsonProperty(PROP_ROLES)
  public @Nonnull SortedSet<NodeRole> getRoles() {
    return _roles;
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull Type getType() {
    return _type;
  }

  /**
   * Create a map from each node name to its set of roles
   *
   * @param nodeNames The universe of nodes that we need to classify
   * @return The created map
   */
  public SortedMap<String, SortedSet<String>> createNodeRolesMap(Set<String> nodeNames) {
    SortedMap<String, SortedSet<String>> nodeRolesMap = new TreeMap<>();
    for (String node : nodeNames) {
      for (NodeRole role : _roles) {
        if (role.matches(node)) {
          SortedSet<String> nodeRoles = nodeRolesMap.computeIfAbsent(node, k -> new TreeSet<>());
          nodeRoles.add(role.getName());
        }
      }
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
    for (NodeRole role : _roles) {
      for (String node : nodeNames) {
        if (role.matches(node)) {
          SortedSet<String> roleNodes =
              roleNodesMap.computeIfAbsent(role.getName(), k -> new TreeSet<>());
          roleNodes.add(node);
        }
      }
    }
    return roleNodesMap;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_ROLE_REGEXES, _roleRegexes)
        .add(PROP_ROLES, _roles)
        .add(PROP_TYPE, _type)
        .toString();
  }
}
