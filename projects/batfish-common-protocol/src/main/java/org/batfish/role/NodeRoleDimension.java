package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.BatfishException;

public class NodeRoleDimension {

  private Map<String, NodeRole> _nodeRoles;

  @JsonCreator
  public NodeRoleDimension(Map<String, NodeRole> nodeRoles) {
    _nodeRoles = nodeRoles;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof NodeRoleDimension)) {
      return false;
    }
    return _nodeRoles.equals(((NodeRoleDimension) o)._nodeRoles);
  }

  @JsonValue
  public Map<String, NodeRole> getNodeRoles() {
    return _nodeRoles;
  }

  // return a map from each role name to the set of nodes that play that role
  public SortedMap<String, SortedSet<String>> createRoleNodesMap(Set<String> nodes) {

    SortedMap<String, SortedSet<String>> roleNodesMap = new TreeMap<>();
    for (Map.Entry<String, NodeRole> entry : _nodeRoles.entrySet()) {
      String roleName = entry.getKey();
      String roleRegex = entry.getValue().toString();
      Pattern pattern;
      try {
        pattern = Pattern.compile(roleRegex);
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            "Supplied regex is not a valid Java regex: \"" + roleRegex + "\"", e);
      }
      for (String node : nodes) {
        Matcher matcher = pattern.matcher(node);
        if (matcher.matches()) {
          SortedSet<String> currNodes =
              roleNodesMap.computeIfAbsent(roleName, k -> new TreeSet<>());
          currNodes.add(node);
        }
      }
    }
    return roleNodesMap;
  }
}
