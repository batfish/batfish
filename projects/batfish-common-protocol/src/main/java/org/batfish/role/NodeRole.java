package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Describes a role played by a node */
@ParametersAreNonnullByDefault
public class NodeRole implements Comparable<NodeRole> {

  private static final String PROP_NAME = "name";
  private static final String PROP_REGEX = "regex";

  @Nonnull private final transient Pattern _compiledPattern;

  @Nonnull private final String _name;

  @Nonnull private final String _regex;

  @JsonCreator
  public NodeRole(@JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_REGEX) String regex) {
    _name = name;
    _regex = regex;
    _compiledPattern = Pattern.compile(regex);
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
}
