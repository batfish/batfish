package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeRoleDimension implements Comparable<NodeRoleDimension> {

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

  @Nonnull private String _name;

  /**
   * a list of regexes used to identify roles from node names. each regex in regexes has at least
   * one group in it that locates the role name within a node name. there are multiple regexes to
   * handle node names that have different formats, and to allow a node to have multple roles. this
   * value is usually populated by auto role inference and may be null for custom role dimensions.
   */
  @Nullable private List<String> _roleRegexes;

  @Nonnull private SortedSet<NodeRole> _roles;

  @Nonnull private Type _type;

  @JsonCreator
  public NodeRoleDimension(
      @Nonnull @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_ROLES) SortedSet<NodeRole> roles,
      @Nullable @JsonProperty(PROP_TYPE) Type type,
      @Nullable @JsonProperty(PROP_ROLE_REGEXES) List<String> roleRegexes) {
    checkArgument(name != null, "Name of node role cannot be null");
    _name = name;
    _roles = firstNonNull(roles, ImmutableSortedSet.of());
    _type = firstNonNull(type, Type.CUSTOM);
    _roleRegexes = roleRegexes;
    if (_type == Type.CUSTOM && _name.startsWith(AUTO_DIMENSION_PREFIX)) {
      throw new IllegalArgumentException(
          "Name for a CUSTOM role dimension cannot begin with: " + AUTO_DIMENSION_PREFIX);
    }
    if (_type == Type.AUTO && !_name.startsWith(AUTO_DIMENSION_PREFIX)) {
      throw new IllegalArgumentException(
          "Name for a AUTO role dimension must begin with: " + AUTO_DIMENSION_PREFIX);
    }
  }

  @Override
  public int compareTo(NodeRoleDimension o) {
    return _name.compareTo(o._name);
  }

  /** If names are equal the NodeRoleDimension objects are considered equal */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRoleDimension)) {
      return false;
    }
    NodeRoleDimension other = (NodeRoleDimension) o;
    return Objects.equals(_name, other._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE_REGEXES)
  public List<String> getRoleRegexes() {
    return _roleRegexes;
  }

  @JsonProperty(PROP_ROLES)
  public SortedSet<NodeRole> getRoles() {
    return _roles;
  }

  @JsonProperty(PROP_TYPE)
  public Type getType() {
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
}
