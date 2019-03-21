package org.batfish.question.testroutepolicies;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment.Direction;

/** A question for testing routing policies. */
@ParametersAreNonnullByDefault
public final class TestRoutePoliciesQuestion extends Question {
  private static final String PROP_DIRECTION = "direction";
  private static final String PROP_INPUT_ROUTE = "inputRoute";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";

  // Defaults are dummy values for now.
  private static final Direction DEFAULT_DIRECTION = Direction.IN;
  private static final BgpRoute DEFAULT_INPUT_ROUTE =
      BgpRoute.builder()
          .setNetwork(Prefix.ZERO)
          .setOriginatorIp(Ip.ZERO)
          .setOriginType(OriginType.INCOMPLETE)
          .setProtocol(RoutingProtocol.BGP)
          .build();
  private static final String DEFAULT_NODE = "";
  private static final String DEFAULT_POLICY = "";

  private final Direction _direction;
  private final String _nodes;
  private final String _policies;
  private final BgpRoute _inputRoute;

  public TestRoutePoliciesQuestion() {
    this(DEFAULT_DIRECTION, DEFAULT_INPUT_ROUTE, DEFAULT_NODE, DEFAULT_POLICY);
  }

  public TestRoutePoliciesQuestion(
      @JsonProperty(PROP_DIRECTION) Direction direction,
      @JsonProperty(PROP_INPUT_ROUTE) BgpRoute inputRoute,
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_POLICIES) String policies) {
    _direction = direction;
    _nodes = nodes;
    _policies = policies;
    _inputRoute = inputRoute;
  }

  @JsonCreator
  private static TestRoutePoliciesQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_DIRECTION) Direction direction,
      @Nullable @JsonProperty(PROP_INPUT_ROUTE) BgpRoute inputRoute,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_POLICIES) String policies) {
    checkNotNull(direction, "%s must not be null", PROP_DIRECTION);
    checkNotNull(inputRoute, "%s must not be null", PROP_INPUT_ROUTE);
    checkNotNull(nodes, "%s must not be null", PROP_NODES);
    checkNotNull(policies, "%s must not be null", PROP_POLICIES);
    return new TestRoutePoliciesQuestion(direction, inputRoute, nodes, policies);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_DIRECTION)
  public Direction getDirection() {
    return _direction;
  }

  @JsonProperty(PROP_INPUT_ROUTE)
  public BgpRoute getInputRoute() {
    return _inputRoute;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return "testRoutePolicies";
  }

  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public String getPolicies() {
    return _policies;
  }
}
