package org.batfish.minesweeper.question.transferbddvalidation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/**
 * A question for validating symbolic route analysis ({@link
 * org.batfish.minesweeper.bdd.TransferBDD}) against concrete route simulation ({@link
 * org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}).
 */
@ParametersAreNonnullByDefault
public final class TransferBDDValidationQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";

  private final @Nullable String _nodes;
  private final @Nullable String _policies;

  public TransferBDDValidationQuestion() {
    this(null, null);
  }

  public TransferBDDValidationQuestion(@Nullable String nodes, @Nullable String policies) {
    _nodes = nodes;
    _policies = policies;
  }

  @JsonCreator
  private static TransferBDDValidationQuestion jsonCreator(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_POLICIES) @Nullable String policies) {
    return new TransferBDDValidationQuestion(nodes, policies);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonIgnore
  @Override
  public @Nonnull String getName() {
    return "transferBDDValidation";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public @Nullable String getPolicies() {
    return _policies;
  }
}
