package org.batfish.question.testroutepolicies;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpSessionProperties;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment.Direction;

/** A question for testing routing policies. */
@ParametersAreNonnullByDefault
public final class TestRoutePoliciesQuestion extends Question {
  private static final String PROP_DIRECTION = "direction";
  private static final String PROP_INPUT_ROUTES = "inputRoutes";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";

  private static final String PROP_BGP_SESSION_PROPERTIES = "bgpSessionProperties";

  // Defaults are dummy values for now.
  private static final Direction DEFAULT_DIRECTION = Direction.IN;
  private static final List<BgpRoute> DEFAULT_INPUT_ROUTES = ImmutableList.of();

  private final @Nonnull Direction _direction;
  private final @Nullable String _nodes;
  private final @Nullable String _policies;
  private final @Nonnull List<BgpRoute> _inputRoutes;

  private final @Nullable BgpSessionProperties _bgpSessionProperties;

  public TestRoutePoliciesQuestion() {
    this(DEFAULT_DIRECTION, DEFAULT_INPUT_ROUTES, null, null, null);
  }

  public TestRoutePoliciesQuestion(
      Direction direction,
      List<BgpRoute> inputRoutes,
      @Nullable String nodes,
      @Nullable String policies,
      @Nullable BgpSessionProperties bgpSessionProperties) {
    _direction = direction;
    _nodes = nodes;
    _policies = policies;
    _inputRoutes = inputRoutes;
    _bgpSessionProperties = bgpSessionProperties;
  }

  @JsonCreator
  private static TestRoutePoliciesQuestion jsonCreator(
      @JsonProperty(PROP_DIRECTION) @Nullable Direction direction,
      @JsonProperty(PROP_INPUT_ROUTES) @Nullable List<BgpRoute> inputRoute,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_POLICIES) @Nullable String policies,
      @JsonProperty(PROP_BGP_SESSION_PROPERTIES) @Nullable
          BgpSessionProperties bgpSessionProperties) {
    checkNotNull(direction, "%s must not be null", PROP_DIRECTION);
    checkNotNull(inputRoute, "%s must not be null", PROP_INPUT_ROUTES);
    return new TestRoutePoliciesQuestion(
        direction, inputRoute, nodes, policies, bgpSessionProperties);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_DIRECTION)
  public @Nonnull Direction getDirection() {
    return _direction;
  }

  @JsonProperty(PROP_INPUT_ROUTES)
  public @Nonnull List<BgpRoute> getInputRoutes() {
    return _inputRoutes;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return "testRoutePolicies";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public @Nullable String getPolicies() {
    return _policies;
  }

  @JsonProperty(PROP_BGP_SESSION_PROPERTIES)
  public @Nullable BgpSessionProperties getBgpSessionProperties() {
    return _bgpSessionProperties;
  }
}
