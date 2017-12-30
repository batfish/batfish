package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;

public class NodesSpecifier {

  public enum Type {
    NAME,
    ROLE
  }

  private String _expression;

  private Pattern _regex;

  private Type _type;

  public NodesSpecifier() {
    this(".*"); // include everything by default
  }

  /*
   * Currently supported example specifiers:
   *   lhr-*      —> all nodes with matching names  (consistent with today)
   *   name:lhr-* -> same as above; name: is optional
   *   role:*srv* —> all nodes where any role matches *srv*
   *
   * In the future, we might need other tags (e.g., loc:) and boolean expressions
   * (e.g., role:*srv* AND lhr-* for all servers with matching names)
   */

  public NodesSpecifier(String expression) {
    _expression = expression;

    String[] parts = expression.split(":");

    if (parts.length == 1) {
      _type = Type.NAME;
      _regex = Pattern.compile(_expression);
    } else if (parts.length == 2) {
      _type = Type.valueOf(parts[0].toUpperCase());
      _regex = Pattern.compile(parts[1]);
    } else {
      throw new IllegalArgumentException("Cannot parse node specifier " + expression);
    }
  }

  @JsonIgnore
  public Set<String> getMatchingNodes(Map<String, Configuration> configurations) {
    Set<String> nodes = new TreeSet<>();
    for (String node : configurations.keySet()) {
      switch (_type) {
        case NAME:
          if (_regex.matcher(node).matches()) {
            nodes.add(node);
          }
          break;
        case ROLE:
          for (String role : configurations.get(node).getRoles()) {
            if (_regex.matcher(role).matches()) {
              nodes.add(node);
            }
          }
          break;
        default:
          throw new BatfishException("Unhandled NodesSpecifier type: " + _type);
      }
    }
    return nodes;
  }

  @JsonValue
  public String toString() {
    return _expression;
  }
}
