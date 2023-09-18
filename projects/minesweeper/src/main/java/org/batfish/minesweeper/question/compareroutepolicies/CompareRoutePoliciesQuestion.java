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
      @Nullable @JsonProperty(PROP_DIRECTION) Environment.Direction direction,
      @Nullable @JsonProperty(PROP_POLICY) String policy,
      @Nullable @JsonProperty(PROP_REFERENCE_POLICY) String referencePolicy,
      @Nullable @JsonProperty(PROP_NODES) String nodes) {
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
  @Nonnull
  @Override
  public String getName() {
    return "compareRoutePolicies";
  }

  @Nullable
  @JsonProperty(PROP_POLICY)
  public String getPolicy() {
    return _policy;
  }

  @Nullable
  @JsonProperty(PROP_REFERENCE_POLICY)
  public String getReferencePolicy() {
    return _referencePolicy;
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }
}
