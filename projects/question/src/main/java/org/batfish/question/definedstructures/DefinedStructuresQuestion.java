package org.batfish.question.definedstructures;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * Fetches all defined structures in config files. The nodeRegex parameter controls structures of
 * which nodes are fetched.
 */
public class DefinedStructuresQuestion extends Question {

  private static final String PROP_NODE_REGEX = "nodeRegex";

  private NodesSpecifier _nodeRegex;

  public DefinedStructuresQuestion(@JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
    _nodeRegex = nodeRegex == null ? NodesSpecifier.ALL : nodeRegex;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "definedstructures";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }
}
