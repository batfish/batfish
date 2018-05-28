package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Describes a role played by a node */
@ParametersAreNonnullByDefault
public class NodeRole implements Comparable<NodeRole> {

  private static final String PROP_NAME = "name";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_REGEX = "regex";

  @Nonnull private final transient Pattern _compiledPattern;

  @Nonnull private final String _name;

  /**
   * We auto generate this field and rehydrate on demand, but we do not make it transient so that it
   * can be serialized by Jackson
   */
  @Nullable private Set<String> _nodes;

  @Nonnull private final String _regex;

  public NodeRole(String name, String regex) {
    this(name, regex, null);
  }

  @JsonCreator
  public NodeRole(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_REGEX) String regex,
      @Nullable @JsonProperty(PROP_NODES) Set<String> nodes) {
    _name = name;
    _regex = regex;
    _compiledPattern = Pattern.compile(regex);
    resetNodes(firstNonNull(nodes, ImmutableSet.of()));
  }

  @Override
  public int compareTo(NodeRole o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof NodeRole)) {
      return false;
    }
    return Objects.equals(_name, ((NodeRole) o)._name)
        && Objects.equals(_regex, ((NodeRole) o)._regex);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_NODES)
  public Set<String> getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REGEX)
  public String getRegex() {
    return _regex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _regex);
  }

  /**
   * Is the provided node name belong to this role?
   *
   * @param nodeName The nodeName to test
   * @return The results of the test
   */
  public boolean matches(String nodeName) {
    return _compiledPattern.matcher(nodeName).matches();
  }

  /**
   * Populates the internal node set as the subset of {@code fromNodes} that match the role.
   *
   * @param fromNodes The set of nodes to pick from
   */
  public void resetNodes(Set<String> fromNodes) {
    _nodes =
        fromNodes.stream().filter(node -> matches(node)).collect(ImmutableSet.toImmutableSet());
  }
}
