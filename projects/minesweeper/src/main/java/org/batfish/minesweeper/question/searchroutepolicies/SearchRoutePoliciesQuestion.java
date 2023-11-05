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
import org.batfish.datamodel.LineAction;
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

  @VisibleForTesting static final LineAction DEFAULT_ACTION = LineAction.PERMIT;

  @VisibleForTesting static final PathOption DEFAULT_PATH_OPTION = PathOption.SINGLE;

  @VisibleForTesting
  static final Environment.Direction DEFAULT_DIRECTION = Environment.Direction.IN;

  private final @Nonnull Environment.Direction _direction;
  private final @Nullable String _nodes;
  private final @Nullable String _policies;
  private final @Nonnull BgpRouteConstraints _inputConstraints;
  private final @Nonnull BgpRouteConstraints _outputConstraints;
  private final @Nonnull LineAction _action;

  private final @Nonnull PathOption _pathOption;

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
      LineAction action,
      PathOption pathOption) {
    checkArgument(
        action == LineAction.PERMIT || outputConstraints.equals(DEFAULT_ROUTE_CONSTRAINTS),
        "Output route constraints can only be provided when the action is 'permit'");
    checkArgument(
        inputConstraints.getAsPath().getRegexConstraints().stream()
            .allMatch(rc -> rc.getRegexType() == RegexConstraint.RegexType.REGEX),
        "AS-path constraints must be AS-path literals or regexes");
    checkArgument(
        outputConstraints.getAsPath().getRegexConstraints().stream()
            .allMatch(rc -> rc.getRegexType() == RegexConstraint.RegexType.REGEX),
        "AS-path constraints must be AS-path literals or regexes");
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
      @JsonProperty(PROP_DIRECTION) @Nullable Environment.Direction direction,
      @JsonProperty(PROP_INPUT_CONSTRAINTS) @Nullable BgpRouteConstraints inputConstraints,
      @JsonProperty(PROP_OUTPUT_CONSTRAINTS) @Nullable BgpRouteConstraints outputConstraints,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_POLICIES) @Nullable String policies,
      @JsonProperty(PROP_ACTION) @Nullable LineAction action,
      @JsonProperty(PROP_PER_PATH) @Nullable Boolean perPath,
      @JsonProperty(PROP_PATH_OPTION) @Nullable PathOption pathOption) {
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
  public @Nonnull Environment.Direction getDirection() {
    return _direction;
  }

  @JsonProperty(PROP_INPUT_CONSTRAINTS)
  public @Nonnull BgpRouteConstraints getInputConstraints() {
    return _inputConstraints;
  }

  @JsonProperty(PROP_OUTPUT_CONSTRAINTS)
  public @Nonnull BgpRouteConstraints getOutputConstraints() {
    return _outputConstraints;
  }

  @JsonIgnore
  @Override
  public @Nonnull String getName() {
    return "searchRoutePolicies";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public @Nullable String getPolicies() {
    return _policies;
  }

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_PATH_OPTION)
  public @Nonnull PathOption getPathOption() {
    return _pathOption;
  }
}
