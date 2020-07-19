package org.batfish.minesweeper.question.searchroutepolicies;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  private static final RouteConstraints DEFAULT_ROUTE_CONSTRAINTS =
      RouteConstraints.builder().build();
  private static final String DEFAULT_NODES = "";
  private static final String DEFAULT_POLICIES = "";
  private static final Action DEFAULT_ACTION = Action.PERMIT;

  private final String _nodes;
  private final String _policies;
  private final RouteConstraints _inputConstraints;
  private final RouteConstraints _outputConstraints;
  private final Action _action;

  public enum Action {
    DENY,
    PERMIT
  }

  public SearchRoutePoliciesQuestion() {
    this(
        DEFAULT_ROUTE_CONSTRAINTS,
        DEFAULT_ROUTE_CONSTRAINTS,
        DEFAULT_NODES,
        DEFAULT_POLICIES,
        DEFAULT_ACTION);
  }

  public SearchRoutePoliciesQuestion(
      @JsonProperty(PROP_INPUT_CONSTRAINTS) RouteConstraints inputConstraints,
      @JsonProperty(PROP_OUTPUT_CONSTRAINTS) RouteConstraints outputConstraints,
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_POLICIES) String policies,
      @JsonProperty(PROP_ACTION) Action action) {
    _nodes = nodes;
    _policies = policies;
    _inputConstraints = inputConstraints;
    _outputConstraints = outputConstraints;
    _action = action;
  }

  @JsonCreator
  private static SearchRoutePoliciesQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_INPUT_CONSTRAINTS) RouteConstraints inputConstraints,
      @Nullable @JsonProperty(PROP_OUTPUT_CONSTRAINTS) RouteConstraints outputConstraints,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_POLICIES) String policies,
      @Nullable @JsonProperty(PROP_ACTION) Action action) {
    return new SearchRoutePoliciesQuestion(
        firstNonNull(inputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        firstNonNull(outputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        firstNonNull(nodes, DEFAULT_NODES),
        firstNonNull(policies, DEFAULT_POLICIES),
        firstNonNull(action, DEFAULT_ACTION));
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_INPUT_CONSTRAINTS)
  public RouteConstraints getInputConstraints() {
    return _inputConstraints;
  }

  @JsonProperty(PROP_OUTPUT_CONSTRAINTS)
  public RouteConstraints getOutputConstraints() {
    return _outputConstraints;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return "searchroutepolicies";
  }

  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public String getPolicies() {
    return _policies;
  }

  @JsonProperty(PROP_ACTION)
  public Action getAction() {
    return _action;
  }
}
