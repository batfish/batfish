package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.datamodel.NodeRoleSpecifier;

/** Represents an inferred policy for the network that all nodes belonging to the same role,
 * according to a given node-role mapping, should have equal values of a given
 * property within the node.
 */
public class RoleConsistencyPolicy {

  private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

  private static final String PROP_NAME = "name";

  private NodeRoleSpecifier _nodeRoleSpecifier;

  /** The name of the configuration property. */
  private String _name;

  @JsonCreator
  public RoleConsistencyPolicy(
      @JsonProperty(PROP_ROLE_SPECIFIER) NodeRoleSpecifier nodeRoleSpecifier,
      @JsonProperty(PROP_NAME) String name) {
    _nodeRoleSpecifier = nodeRoleSpecifier;
    _name = name;
  }


  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE_SPECIFIER)
  public NodeRoleSpecifier getNodeRoleSpecifier() {
    return _nodeRoleSpecifier;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeRoleSpecifier, _name);
  }
}
