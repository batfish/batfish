package org.batfish.question.testroutepolicies;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.trace.TraceTree;

/**
 * A {@link TestRoutePoliciesQuestion} result for a single policy and input route. The class is
 * parameterized by the types used for the input and output routes.
 */
public class Result<I, O> {
  /** A key to relate results by policy and input route. */
  public static final class Key<R> {
    private final RoutingPolicyId _policyId;
    private final R _inputRoute;

    public Key(RoutingPolicyId policyId, R inputRoute) {
      _policyId = policyId;
      _inputRoute = inputRoute;
    }

    public @Nonnull R getInputRoute() {
      return _inputRoute;
    }

    public @Nonnull RoutingPolicyId getPolicyId() {
      return _policyId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key<?>)) {
        return false;
      }
      Key<?> key = (Key<?>) o;
      return Objects.equals(_policyId, key._policyId)
          && Objects.equals(_inputRoute, key._inputRoute);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_policyId, _inputRoute);
    }
  }

  /**
   * A type to represent different attributes of a route, which is used to identify particular
   * attributes of the input route that are relevant for this result.
   */
  public enum RouteAttributeType {
    ADMINISTRATIVE_DISTANCE,
    AS_PATH,
    CLUSTER_LIST,
    COMMUNITIES,
    LOCAL_PREFERENCE,
    METRIC,
    NETWORK,
    NEXT_HOP,
    ORIGIN_TYPE,
    PROTOCOL,
    TAG,
    TUNNEL_ENCAPSULATION_ATTRIBUTE,
    WEIGHT
  }

  private final RoutingPolicyId _policyId;
  private final I _inputRoute;

  /**
   * If non-null, this list contains the attributes of the input route that are relevant for the
   * behavior exhibited by this result. If null, then the relevant attributes have not been
   * computed.
   */
  private @Nullable List<RouteAttributeType> _relevantInputAttributes;

  private final LineAction _action;
  private final @Nullable O _outputRoute;
  private final List<TraceTree> _trace;

  Result(
      RoutingPolicyId policyId,
      I inputRoute,
      LineAction action,
      @Nullable O outputRoute,
      List<TraceTree> traceTrees) {
    checkArgument(
        (action == LineAction.DENY) == (outputRoute == null),
        "outputRoute is null if and only if action is DENY");
    _policyId = policyId;
    _action = action;
    _inputRoute = inputRoute;
    _outputRoute = outputRoute;
    _trace = traceTrees;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Result<?, ?> result)) {
      return false;
    }
    return Objects.equals(_policyId, result._policyId)
        && _action == result._action
        && Objects.equals(_inputRoute, result._inputRoute)
        && Objects.equals(_relevantInputAttributes, result._relevantInputAttributes)
        && Objects.equals(_outputRoute, result._outputRoute)
        && Objects.equals(_trace, result._trace);
  }

  public LineAction getAction() {
    return _action;
  }

  public I getInputRoute() {
    return _inputRoute;
  }

  /**
   * If non-null, the returned list contains the attributes of the input route that are relevant for
   * the behavior exhibited by this result. The key property is that for any attribute A that is not
   * in the returned list, any concrete value of the appropriate type can be used for A without
   * affecting the result's behavior. If null, then the relevant attributes have not been computed.
   */
  public @Nullable List<RouteAttributeType> getRelevantInputAttributes() {
    return _relevantInputAttributes;
  }

  public void setRelevantInputAttributes(List<RouteAttributeType> attributes) {
    _relevantInputAttributes = attributes;
  }

  public <ONew> Result<I, ONew> setOutputRoute(ONew outputRoute) {
    return new Result<>(_policyId, _inputRoute, _action, outputRoute, _trace);
  }

  public Key<I> getKey() {
    return new Key<>(_policyId, _inputRoute);
  }

  public @Nullable O getOutputRoute() {
    return _outputRoute;
  }

  public RoutingPolicyId getPolicyId() {
    return _policyId;
  }

  public List<TraceTree> getTrace() {
    return _trace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _policyId, _action, _inputRoute, _relevantInputAttributes, _outputRoute, _trace);
  }
}
