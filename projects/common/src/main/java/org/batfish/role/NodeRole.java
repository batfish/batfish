package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

/** Describes a role played by a node */
@ParametersAreNonnullByDefault
public class NodeRole implements Comparable<NodeRole> {
  private static final String PROP_NAME = "name";
  private static final String PROP_REGEX = "regex";

  private final @Nonnull transient Pattern _compiledPattern;

  private final @Nonnull String _name;

  private final @Nonnull String _regex;

  @JsonCreator
  private static NodeRole create(
      @JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_REGEX) String regex) {
    if (name == null) {
      throw new IllegalArgumentException("Node role name cannot be null");
    }
    if (regex == null) {
      throw new IllegalArgumentException("Node role regex cannot be null");
    }
    return new NodeRole(name, regex);
  }

  public NodeRole(String name, String regex) {
    Names.checkName(name, "role", Type.REFERENCE_OBJECT);
    _name = name;
    _regex = regex;
    try {
      _compiledPattern = Pattern.compile(regex);
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException("Bad regex: " + e.getMessage());
    }
  }

  @Override
  public int compareTo(NodeRole o) {
    return Comparator.comparing(NodeRole::getName)
        .thenComparing(NodeRole::getRegex)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRole)) {
      return false;
    }
    return Objects.equals(_name, ((NodeRole) o)._name)
        && Objects.equals(_regex, ((NodeRole) o)._regex);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
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
   * Does the provided node name belong to this role?
   *
   * @param nodeName The nodeName to test
   * @return The results of the test
   */
  public boolean matches(String nodeName) {
    return _compiledPattern.matcher(nodeName).matches();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("name", _name)
        .add("regex", _regex)
        .toString();
  }
}
