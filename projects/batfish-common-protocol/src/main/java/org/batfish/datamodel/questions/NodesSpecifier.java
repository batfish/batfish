package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;

/**
 * Enables specification of groups of nodes in various questions.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>lhr-.* —> all nodes with matching names (old style)
 *   <li>name:lhr-.* -> same as above; name: is optional
 *   <li>role:srv.* —> all nodes where any role matches the pattern
 * </ul>
 *
 * <p>In the future, we might need other tags (e.g., loc:) and boolean expressions (e.g., role:srv.*
 * AND lhr-* for all servers with matching names)
 */
public class NodesSpecifier {

  public enum Type {
    NAME,
    ROLE
  }

  public static NodesSpecifier ALL = new NodesSpecifier(".*");

  private final String _expression;

  private final Pattern _regex;

  private final Type _type;

  public NodesSpecifier(String expression) {
    _expression = expression;

    String[] parts = expression.split(":");

    if (parts.length == 1) {
      _type = Type.NAME;
      _regex = Pattern.compile(_expression);
    } else if (parts.length == 2) {
      try {
        _type = Type.valueOf(parts[0].toUpperCase());
        _regex = Pattern.compile(parts[1]);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Illegal NodesSpecifier filter " + parts[1] + ".  Should be one of 'name' or 'role'");
      }
    } else {
      throw new IllegalArgumentException("Cannot parse NodeSpecifier " + expression);
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

  @JsonIgnore
  public Pattern getRegex() {
    return _regex;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @JsonValue
  public String toString() {
    return _expression;
  }
}
