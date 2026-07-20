package org.batfish.minesweeper.question.comparepeergrouppolicies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

public class ComparePeerGroupPoliciesQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  private final @Nullable String _nodes;

  public ComparePeerGroupPoliciesQuestion() {
    this(null);
  }

  @JsonCreator
  public ComparePeerGroupPoliciesQuestion(@JsonProperty(PROP_NODES) @Nullable String nodes) {
    _nodes = nodes;
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return "SemDiff";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
