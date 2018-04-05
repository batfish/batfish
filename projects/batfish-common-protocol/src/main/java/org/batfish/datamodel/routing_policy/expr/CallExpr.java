package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class CallExpr extends BooleanExpr {

  private static final String PROP_CALLED_POLICY_NAME = "calledPolicyName";

  /** */
  private static final long serialVersionUID = 1L;

  private String _calledPolicyName;

  @JsonCreator
  private CallExpr() {}

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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CallExpr other = (CallExpr) obj;
    if (_calledPolicyName == null) {
      if (other._calledPolicyName != null) {
        return false;
      }
    } else if (!_calledPolicyName.equals(other._calledPolicyName)) {
      return false;
    }
    return true;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_calledPolicyName == null) ? 0 : _calledPolicyName.hashCode());
    return result;
  }

  @JsonProperty(PROP_CALLED_POLICY_NAME)
  public void setCalledPolicyName(String calledPolicyName) {
    _calledPolicyName = calledPolicyName;
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_CALLED_POLICY_NAME, _calledPolicyName).toString();
  }
}
