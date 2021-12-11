package org.batfish.question.a10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns VIP configuration of A10 devices. */
public class A10VirtualServerConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";

  @Nullable private String _nodes;

  @JsonCreator
  public A10VirtualServerConfigurationQuestion(@JsonProperty(PROP_NODES) @Nullable String nodes) {
    _nodes = nodes;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "a10VirtualServerConfiguration";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
