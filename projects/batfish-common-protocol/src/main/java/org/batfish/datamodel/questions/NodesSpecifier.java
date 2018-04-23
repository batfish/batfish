package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;

/**
 * Enables specification of groups of nodes in various questions.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>lhr-.* —> all nodes with matching names (old style)
 *   <li>name:lhr-.* -> same as above; name: is optional
 *   <li>role:auto0:a1.* —> all nodes with roles that match a1.* in role dimension auto0
 * </ul>
 *
 * <p>In the future, we might need other tags (e.g., loc:) and boolean expressions (e.g.,
 * role:auto1.a1 AND lhr-* for all servers with matching names)
 */
public class NodesSpecifier {

  public enum Type {
    NAME,
    ROLE
  }

  public static final NodesSpecifier ALL = new NodesSpecifier(".*");

  public static final NodesSpecifier NONE = new NodesSpecifier("");

  @Nonnull private final String _expression;

  @Nonnull private final Pattern _regex;

  @Nullable private final String _roleDimension;

  @Nonnull private final Type _type;

  public NodesSpecifier(String expression) {
    _expression = expression;

    String[] parts = expression.split(":");

    if (parts.length == 1) {
      _type = Type.NAME;
      _regex = Pattern.compile(_expression);
      _roleDimension = null;
    } else {
      try {
        _type = Type.valueOf(parts[0].toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format(
                "Illegal NodesSpecifier filter %s. Should be one of %s",
                parts[0],
                Arrays.stream(Type.values())
                    .map(v -> v.toString())
                    .collect(Collectors.joining(", "))));
      }
      if (parts.length == 2 && _type == Type.NAME) {
        _regex = Pattern.compile(parts[1]);
        _roleDimension = null;
      } else if (parts.length == 3 && _type == Type.ROLE) {
        _roleDimension = parts[1];
        _regex = Pattern.compile(parts[2]);
      } else {
        throw new IllegalArgumentException("Cannot parse NodeSpecifier " + expression);
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NodesSpecifier)) {
      return false;
    }
    NodesSpecifier rhs = (NodesSpecifier) obj;
    return Objects.equals(_expression, rhs._expression)
        && Objects.equals(_regex.pattern(), rhs._regex.pattern())
        && Objects.equals(_type, rhs._type);
  }

  @JsonIgnore
  public Set<String> getMatchingNodes(IBatfish batfish) {
    return getMatchingNodes(batfish, batfish.loadConfigurations().keySet());
  }

  @JsonIgnore
  private Set<String> getMatchingNodes(IBatfish batfish, Set<String> nodes) {
    switch (_type) {
      case NAME:
        return getMatchingNodesByName(nodes);
      case ROLE:
        NodeRoleDimension roleDimension = batfish.getNodeRoleDimension(_roleDimension);
        return getMatchingNodesByRole(roleDimension, nodes);
      default:
        throw new BatfishException("Unhandled NodesSpecifier type: " + _type);
    }
  }

  @VisibleForTesting
  public Set<String> getMatchingNodesByName(Set<String> nodes) {
    return nodes.stream().filter(n -> _regex.matcher(n).matches()).collect(Collectors.toSet());
  }

  @VisibleForTesting
  public Set<String> getMatchingNodesByRole(NodeRoleDimension roleDimension, Set<String> nodes) {
    return nodes
        .stream()
        .filter(n -> nodeNameInMatchingRole(n, roleDimension.getRoles()))
        .collect(Collectors.toSet());
  }

  @JsonIgnore
  public Pattern getRegex() {
    return _regex;
  }

  @JsonIgnore
  public String getRoleDimension() {
    return _roleDimension;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expression, _regex.pattern(), _type.ordinal());
  }

  /** Does this nodeName match any of roles that match our _regex? */
  private boolean nodeNameInMatchingRole(String nodeName, Set<NodeRole> roles) {
    for (NodeRole role : roles) {
      if (_regex.matcher(role.getName()).matches() && role.matches(nodeName)) {
        return true;
      }
    }
    return false;
  }

  @JsonValue
  public String toString() {
    return _expression;
  }
}
