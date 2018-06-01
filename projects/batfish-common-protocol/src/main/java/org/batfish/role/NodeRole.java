package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/** Describes a role played by a node */
public class NodeRole implements Comparable<NodeRole> {

  private static final String PROP_NAME = "name";

  private static final String PROP_REGEX = "regex";

  private static final String PROP_CASE_SENSITIVE = "caseSensitive";

  @Nonnull private final String _name;

  @Nonnull private final String _regex;

  private final boolean _caseSensitive;

  private final transient Pattern _compiledPattern;

  @JsonCreator
  public NodeRole(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_REGEX) String regex,
      @JsonProperty(PROP_CASE_SENSITIVE) boolean caseSensitive) {
    _name = name;
    _regex = regex;
    _caseSensitive = caseSensitive;
    _compiledPattern = Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
  }

  public NodeRole(@JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_REGEX) String regex) {
    this(name, regex, false);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof NodeRole)) {
      return false;
    }
    return _regex.equals(((NodeRole) o)._regex);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_REGEX)
  public String getRegex() {
    return _regex;
  }

  @JsonProperty(PROP_CASE_SENSITIVE)
  public boolean getCaseSensitive() {
    return _caseSensitive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _regex);
  }

  /**
   * Does the provided node name match the regex of this role?
   *
   * @param nodeName The nodeName to test
   * @return The results of the test
   */
  public boolean matches(String nodeName) {
    return _compiledPattern.matcher(nodeName).matches();
  }

  @Override
  public int compareTo(NodeRole o) {
    return _name.compareTo(o._name);
  }
}
