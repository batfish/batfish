package org.batfish.minesweeper.question.compareroutepolicies;

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

/** A question for comparing routing policies. */
@ParametersAreNonnullByDefault
public final class CompareRoutePoliciesQuestion extends Question {

  private static final String PROP_DIRECTION = "direction";
  private static final String PROP_POLICY = "policy";
  private static final String PROP_REFERENCE_POLICY = "referencePolicy";
  private static final String PROP_NODES = "nodes";

  @VisibleForTesting
  static final Environment.Direction DEFAULT_DIRECTION = Environment.Direction.IN;

  private final @Nonnull Environment.Direction _direction;
  private final @Nullable String _policy;
  private final @Nullable String _referencePolicy;
  private final @Nullable String _nodes;

  public CompareRoutePoliciesQuestion() {
    this(DEFAULT_DIRECTION, null, null, null);
  }

  public CompareRoutePoliciesQuestion(
      Environment.Direction direction,
      @Nullable String policy,
      @Nullable String referencePolicy,
      @Nullable String nodes) {

    _direction = direction;
    _policy = policy;
    _referencePolicy = referencePolicy;
    _nodes = nodes;
    setDifferential(true);
  }

  @JsonCreator
  private static CompareRoutePoliciesQuestion jsonCreator(
      @JsonProperty(PROP_DIRECTION) @Nullable Environment.Direction direction,
      @JsonProperty(PROP_POLICY) @Nullable String policy,
      @JsonProperty(PROP_REFERENCE_POLICY) @Nullable String referencePolicy,
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new CompareRoutePoliciesQuestion(
        firstNonNull(direction, DEFAULT_DIRECTION), policy, referencePolicy, nodes);
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

  @JsonIgnore
  @Override
  public @Nonnull String getName() {
    return "compareRoutePolicies";
  }

  @JsonProperty(PROP_POLICY)
  public @Nullable String getPolicy() {
    return _policy;
  }

  @JsonProperty(PROP_REFERENCE_POLICY)
  public @Nullable String getReferencePolicy() {
    return _referencePolicy;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }
}
