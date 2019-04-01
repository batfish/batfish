package org.batfish.question.testroutepolicies;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.LineAction;

/** A {@link TestRoutePoliciesQuestion} result for a single policy and input route. */
final class Result {
  /** A key to relate results by policy and input route. */
  public final class Key {
    private final RoutingPolicyId _policyId;
    private final BgpRoute _inputRoute;

    private Key(RoutingPolicyId policyId, BgpRoute inputRoute) {
      _policyId = policyId;
      _inputRoute = inputRoute;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key)) {
        return false;
      }
      Key key = (Key) o;
      return Objects.equals(_policyId, key._policyId)
          && Objects.equals(_inputRoute, key._inputRoute);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_policyId, _inputRoute);
    }
  }

  private final RoutingPolicyId _policyId;
  private final BgpRoute _inputRoute;
  private final LineAction _action;
  private final @Nullable BgpRoute _outputRoute;

  Result(
      RoutingPolicyId policyId,
      BgpRoute inputRoute,
      LineAction action,
      @Nullable BgpRoute outputRoute) {
    checkArgument(
        (action == LineAction.DENY) == (outputRoute == null),
        "outputRoute is null if and only if action is DENY");
    _policyId = policyId;
    _action = action;
    _inputRoute = inputRoute;
    _outputRoute = outputRoute;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Result)) {
      return false;
    }
    Result result = (Result) o;
    return Objects.equals(_policyId, result._policyId)
        && _action == result._action
        && Objects.equals(_inputRoute, result._inputRoute)
        && Objects.equals(_outputRoute, result._outputRoute);
  }

  public LineAction getAction() {
    return _action;
  }

  public BgpRoute getInputRoute() {
    return _inputRoute;
  }

  public Key getKey() {
    return new Key(_policyId, _inputRoute);
  }

  @Nullable
  public BgpRoute getOutputRoute() {
    return _outputRoute;
  }

  public RoutingPolicyId getPolicyId() {
    return _policyId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_policyId, _action, _inputRoute, _outputRoute);
  }
}
