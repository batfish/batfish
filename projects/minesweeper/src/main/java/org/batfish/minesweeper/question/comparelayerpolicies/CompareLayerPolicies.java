package org.batfish.minesweeper.question.comparelayerpolicies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question for comparing routing policies. */
@ParametersAreNonnullByDefault
public final class CompareLayerPolicies extends Question {

  private static final String PROP_NODES = "nodes";

  private final @Nullable String _nodes;

  public CompareLayerPolicies() {
    this(null);
  }

  public CompareLayerPolicies(@Nullable String nodes) {
    _nodes = nodes;
  }

  @JsonCreator
  private static CompareLayerPolicies jsonCreator(
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new CompareLayerPolicies(nodes);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonIgnore
  @Override
  public @Nonnull String getName() {
    return "compareLayerPolicies";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }
}
