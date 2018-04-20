package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;

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
}
