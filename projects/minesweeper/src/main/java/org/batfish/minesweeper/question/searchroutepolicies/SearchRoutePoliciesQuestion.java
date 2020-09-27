package org.batfish.minesweeper.question.searchroutepolicies;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question for testing routing policies. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesQuestion extends Question {
  private static final String PROP_INPUT_CONSTRAINTS = "inputConstraints";
  private static final String PROP_OUTPUT_CONSTRAINTS = "outputConstraints";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";
  private static final String PROP_ACTION = "action";

  @VisibleForTesting
  static final BgpRouteConstraints DEFAULT_ROUTE_CONSTRAINTS =
      BgpRouteConstraints.builder().build();

  @VisibleForTesting static final Action DEFAULT_ACTION = Action.PERMIT;

  @Nullable private final String _nodes;
  @Nullable private final String _policies;
  @Nonnull private final BgpRouteConstraints _inputConstraints;
  @Nonnull private final BgpRouteConstraints _outputConstraints;
  @Nonnull private final Action _action;

  public enum Action {
    DENY,
    PERMIT
  }

  public SearchRoutePoliciesQuestion() {
    this(DEFAULT_ROUTE_CONSTRAINTS, DEFAULT_ROUTE_CONSTRAINTS, null, null, DEFAULT_ACTION);
  }

  public SearchRoutePoliciesQuestion(
      BgpRouteConstraints inputConstraints,
      BgpRouteConstraints outputConstraints,
      @Nullable String nodes,
      @Nullable String policies,
      Action action) {
    _nodes = nodes;
    _policies = policies;
    _inputConstraints = inputConstraints;
    _outputConstraints = outputConstraints;
    _action = action;
  }

  @JsonCreator
  private static SearchRoutePoliciesQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_INPUT_CONSTRAINTS) BgpRouteConstraints inputConstraints,
      @Nullable @JsonProperty(PROP_OUTPUT_CONSTRAINTS) BgpRouteConstraints outputConstraints,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_POLICIES) String policies,
      @Nullable @JsonProperty(PROP_ACTION) Action action) {
    return new SearchRoutePoliciesQuestion(
        firstNonNull(inputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        firstNonNull(outputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        nodes,
        policies,
        firstNonNull(action, DEFAULT_ACTION));
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_INPUT_CONSTRAINTS)
  @Nonnull
  public BgpRouteConstraints getInputConstraints() {
    return _inputConstraints;
  }

  @JsonProperty(PROP_OUTPUT_CONSTRAINTS)
  @Nonnull
  public BgpRouteConstraints getOutputConstraints() {
    return _outputConstraints;
  }

  @JsonIgnore
  @Nonnull
  @Override
  public String getName() {
    return "searchRoutePolicies";
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nullable
  @JsonProperty(PROP_POLICIES)
  public String getPolicies() {
    return _policies;
  }

  @JsonProperty(PROP_ACTION)
  @Nonnull
  public Action getAction() {
    return _action;
  }
}
