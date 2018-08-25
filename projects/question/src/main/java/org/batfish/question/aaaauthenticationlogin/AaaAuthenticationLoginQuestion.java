package org.batfish.question.aaaauthenticationlogin;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

public class AaaAuthenticationLoginQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  @Nonnull private NodesSpecifier _nodes;

  public AaaAuthenticationLoginQuestion() {
    this(null);
  }

  public AaaAuthenticationLoginQuestion(@Nullable @JsonProperty(PROP_NODES) NodesSpecifier nodes) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "AaaAuthenticationLogin";
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_NODES)
  public void setNodes(NodesSpecifier nodes) {
    _nodes = nodes;
  }
}
