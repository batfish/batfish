package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.role.OutliersHypothesis;

/**
 * Represents an inferred policy for the network that all nodes belonging to the same role,
 * according to a given node-role mapping, should have equal values of a given property within the
 * node.
 */
public class RoleConsistencyPolicy {
  private static final String PROP_ROLE_DIMENSION = "roleDimension";
  private static final String PROP_NAME = "name";
  private static final String PROP_HYPOTHESIS = "hypothesis";

  private String _nodeRoleDimension;

  /** The name of the configuration property. */
  private String _name;

  /** The kind of consistency policy. */
  private OutliersHypothesis _hypothesis;

  @JsonCreator
  public RoleConsistencyPolicy(
      @JsonProperty(PROP_ROLE_DIMENSION) String nodeRoleDimension,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_HYPOTHESIS) OutliersHypothesis hypothesis) {
    _nodeRoleDimension = nodeRoleDimension;
    _name = name;
    _hypothesis = hypothesis;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE_DIMENSION)
  public String getNodeRoleDimension() {
    return _nodeRoleDimension;
  }

  @JsonProperty(PROP_HYPOTHESIS)
  public OutliersHypothesis getHypothesis() {
    return _hypothesis;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeRoleDimension, _name);
  }
}
