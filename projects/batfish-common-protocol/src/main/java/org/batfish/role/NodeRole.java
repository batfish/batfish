package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public class NodeRole {

  private final String _regex;

  private final transient Pattern _compiledPattern;

  public NodeRole(String regex) {
    _regex = regex;
    _compiledPattern = Pattern.compile(regex);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof NodeRole)) {
      return false;
    }
    return _regex.equals(((NodeRole) o)._regex);
  }

  @JsonValue
  public String toString() {
    return _regex;
  }
}
