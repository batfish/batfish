package org.batfish.question.aaaauthenticationlogin;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

public class AaaAuthenticationLoginQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  @Nonnull private NodeSpecifier _nodes;

  public AaaAuthenticationLoginQuestion() {
    this(null);
  }

  public AaaAuthenticationLoginQuestion(@Nullable @JsonProperty(PROP_NODES) String nodes) {
    _nodes = SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE);
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
  public NodeSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_NODES)
  public void setNodes(NodeSpecifier nodes) {
    _nodes = nodes;
  }
}
