package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.RouteConstraints;
import org.batfish.datamodel.questions.Question;

/** A question for testing routing policies. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesQuestion extends Question {
  private static final String PROP_ROUTE_CONSTRAINTS = "routeConstraints";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";
  private static final String PROP_ACTION = "action";

  private static final RouteConstraints DEFAULT_ROUTE_CONSTRAINTS =
      RouteConstraints.builder().build();
  private static final String DEFAULT_NODE = "";
  private static final String DEFAULT_POLICY = "";
  private static final Action DEFAULT_ACTION = Action.PERMIT;

  private final String _nodes;
  private final String _policies;
  private final RouteConstraints _routeConstraints;
  private final Action _action;

  public enum Action {
    DENY,
    PERMIT
  }

  public SearchRoutePoliciesQuestion() {
    this(DEFAULT_ROUTE_CONSTRAINTS, DEFAULT_NODE, DEFAULT_POLICY, DEFAULT_ACTION);
  }

  public SearchRoutePoliciesQuestion(
      @JsonProperty(PROP_ROUTE_CONSTRAINTS) RouteConstraints routeConstraints,
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_POLICIES) String policies,
      @JsonProperty(PROP_ACTION) Action action) {
    _nodes = nodes;
    _policies = policies;
    _routeConstraints = routeConstraints;
    _action = action;
  }

  @JsonCreator
  private static SearchRoutePoliciesQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_ROUTE_CONSTRAINTS) RouteConstraints routeConstraints,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_POLICIES) String policies,
      @Nullable @JsonProperty(PROP_ACTION) Action action) {
    checkNotNull(routeConstraints, "%s must not be null", PROP_ROUTE_CONSTRAINTS);
    checkNotNull(nodes, "%s must not be null", PROP_NODES);
    checkNotNull(policies, "%s must not be null", PROP_POLICIES);
    checkNotNull(action, "%s must not be null", PROP_ACTION);
    return new SearchRoutePoliciesQuestion(routeConstraints, nodes, policies, action);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_ROUTE_CONSTRAINTS)
  public RouteConstraints getRouteConstraints() {
    return _routeConstraints;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return "searchRoutePolicies";
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
