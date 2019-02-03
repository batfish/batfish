package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Boolean expression that calls a given {@link RoutingPolicy} on an {@link Environment}. */
public final class CallExpr extends BooleanExpr {

  private static final String PROP_CALLED_POLICY_NAME = "calledPolicyName";

  private static final long serialVersionUID = 1L;

  private final String _calledPolicyName;

  @JsonCreator
  private static CallExpr create(@JsonProperty(PROP_CALLED_POLICY_NAME) String calledPolicyName) {
    checkArgument(calledPolicyName != null, "%s must be provided", PROP_CALLED_POLICY_NAME);
    return new CallExpr(calledPolicyName);
  }

  public CallExpr(String includedPolicyName) {
    _calledPolicyName = includedPolicyName;
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    if (parentSources.contains(_calledPolicyName)) {
      w.redFlag(
          "Circular reference to routing policy: '"
              + _calledPolicyName
              + "' detected at expression: '"
              + toString()
              + "'");
      return Collections.emptySet();
    }
    RoutingPolicy calledPolicy = routingPolicies.get(_calledPolicyName);
    if (calledPolicy == null) {
      return Collections.emptySet();
    }
    return calledPolicy.computeSources(parentSources, routingPolicies, w);
  }

  @Override
  public Result evaluate(Environment environment) {
    RoutingPolicy policy =
        environment.getConfiguration().getRoutingPolicies().get(_calledPolicyName);
    Result result;
    if (policy == null) {
      result = new Result();
      environment.setError(true);
      result.setBooleanValue(false);
    } else {
      boolean oldCallExprContext = environment.getCallExprContext();
      boolean oldLocalDefaultAction = environment.getLocalDefaultAction();
      environment.setCallExprContext(true);
      result = policy.call(environment);
      result.setReturn(false);
      environment.setCallExprContext(oldCallExprContext);
      environment.setLocalDefaultAction(oldLocalDefaultAction);
    }
    return result;
  }

  @JsonProperty(PROP_CALLED_POLICY_NAME)
  public String getCalledPolicyName() {
    return _calledPolicyName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CallExpr)) {
      return false;
    }
    CallExpr other = (CallExpr) obj;
    return Objects.equals(_calledPolicyName, other._calledPolicyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_calledPolicyName);
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_CALLED_POLICY_NAME, _calledPolicyName).toString();
  }
}
