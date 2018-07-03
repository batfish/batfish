package org.batfish.question.aaaauthenticationlogin;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

public class AaaAuthenticationLoginQuestion extends Question {

  private static final String PROP_NODE_REGEX = "nodeRegex";

  @Nonnull private NodesSpecifier _nodeRegex;

  public AaaAuthenticationLoginQuestion() {
    this(null);
  }

  public AaaAuthenticationLoginQuestion(
      @Nullable @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "AaaAuthenticationLogin";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public void setNodeRegex(NodesSpecifier nodeRegex) {
    _nodeRegex = nodeRegex;
  }
}
