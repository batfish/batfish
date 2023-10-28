package org.batfish.question.aaaauthenticationlogin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

@ParametersAreNonnullByDefault
public class AaaAuthenticationLoginQuestion extends Question {
  private static final String PROP_NODES = "nodes";

  private @Nullable String _nodes;

  @JsonCreator
  private static AaaAuthenticationLoginQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new AaaAuthenticationLoginQuestion(nodes);
  }

  AaaAuthenticationLoginQuestion() {
    this(null);
  }

  public AaaAuthenticationLoginQuestion(@Nullable String nodes) {
    _nodes = nodes;
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
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
