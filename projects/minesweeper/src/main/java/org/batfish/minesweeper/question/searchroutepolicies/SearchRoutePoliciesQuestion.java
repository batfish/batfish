package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment;

/** A question for testing routing policies. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesQuestion extends Question {

  private static final String PROP_DIRECTION = "direction";
  private static final String PROP_INPUT_CONSTRAINTS = "inputConstraints";
  private static final String PROP_OUTPUT_CONSTRAINTS = "outputConstraints";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";
  private static final String PROP_ACTION = "action";

  private static final String PROP_PER_PATH = "perPath";
  private static final String PROP_PATH_OPTION = "pathOption";

  @VisibleForTesting
  static final BgpRouteConstraints DEFAULT_ROUTE_CONSTRAINTS =
      BgpRouteConstraints.builder().build();

  @VisibleForTesting static final Action DEFAULT_ACTION = Action.PERMIT;

  @VisibleForTesting static final PathOption DEFAULT_PATH_OPTION = PathOption.SINGLE;

  @VisibleForTesting
  static final Environment.Direction DEFAULT_DIRECTION = Environment.Direction.IN;

  @Nonnull private final Environment.Direction _direction;
  @Nullable private final String _nodes;
  @Nullable private final String _policies;
  @Nonnull private final BgpRouteConstraints _inputConstraints;
  @Nonnull private final BgpRouteConstraints _outputConstraints;
  @Nonnull private final Action _action;

  private final PathOption _pathOption;

  public enum Action {
    DENY,
    PERMIT
  }

  /**
   * The PathOption enum represents various options for how results are presented based on how paths
   * are explored.
   */
  public enum PathOption {
    /** SINGLE means we return a single path that meets the input and ouput constraints. */
    SINGLE,

    /**
     * PER_PATH means we return a set of input and output routes that follow each execution path.
     */
    PER_PATH,

    /**
     * NON_OVERLAP means the same as PER_PATH except each input route we return does not conflict
     * with any others.
     */
    NON_OVERLAP
  }

  public SearchRoutePoliciesQuestion() {
    this(
        DEFAULT_DIRECTION,
        DEFAULT_ROUTE_CONSTRAINTS,
        DEFAULT_ROUTE_CONSTRAINTS,
        null,
        null,
        DEFAULT_ACTION,
        DEFAULT_PATH_OPTION);
  }

  public SearchRoutePoliciesQuestion(
      Environment.Direction direction,
      BgpRouteConstraints inputConstraints,
      BgpRouteConstraints outputConstraints,
      @Nullable String nodes,
      @Nullable String policies,
      Action action,
      PathOption pathOption) {
    checkArgument(
        action == Action.PERMIT || outputConstraints.equals(DEFAULT_ROUTE_CONSTRAINTS),
        "Output route constraints can only be provided when the action is 'permit'");
    _direction = direction;
    _nodes = nodes;
    _policies = policies;
    _inputConstraints = inputConstraints;
    _outputConstraints = outputConstraints;
    _action = action;
    _pathOption = pathOption;
  }

  @JsonCreator
  private static SearchRoutePoliciesQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_DIRECTION) Environment.Direction direction,
      @Nullable @JsonProperty(PROP_INPUT_CONSTRAINTS) BgpRouteConstraints inputConstraints,
      @Nullable @JsonProperty(PROP_OUTPUT_CONSTRAINTS) BgpRouteConstraints outputConstraints,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_POLICIES) String policies,
      @Nullable @JsonProperty(PROP_ACTION) Action action,
      @Nullable @JsonProperty(PROP_PER_PATH) Boolean perPath,
      @Nullable @JsonProperty(PROP_PATH_OPTION) PathOption pathOption) {
    checkArgument(
        !(perPath != null && pathOption != null),
        "perPath and pathOption cannot both be set (perPath is deprecated)");
    // if pathOption is null we interpret that is the default analysis type (SINGLE)
    PathOption p = pathOption == null ? PathOption.SINGLE : pathOption;
    return new SearchRoutePoliciesQuestion(
        firstNonNull(direction, DEFAULT_DIRECTION),
        firstNonNull(inputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        firstNonNull(outputConstraints, DEFAULT_ROUTE_CONSTRAINTS),
        nodes,
        policies,
        firstNonNull(action, DEFAULT_ACTION),
        perPath != null && perPath ? PathOption.PER_PATH : p);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_DIRECTION)
  @Nonnull
  public Environment.Direction getDirection() {
    return _direction;
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

  @JsonProperty(PROP_PATH_OPTION)
  @Nonnull
  public PathOption getPathOption() {
    return _pathOption;
  }
}
