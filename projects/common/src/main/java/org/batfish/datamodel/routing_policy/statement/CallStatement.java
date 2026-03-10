package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class CallStatement extends Statement {
  private static final String PROP_CALLED_POLICY_NAME = "calledPolicyName";

  private String _calledPolicyName;

  @JsonCreator
  private CallStatement() {}

  public CallStatement(String includedPolicyName) {
    _calledPolicyName = includedPolicyName;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitCallStatement(this, arg);
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    if (parentSources.contains(_calledPolicyName)) {
      w.redFlag(
          "Circular reference to routing policy: '"
              + _calledPolicyName
              + "' detected at statement: '"
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
    CallStatement other = (CallStatement) obj;
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
  public Result execute(Environment environment) {
    RoutingPolicy policy = environment.getRoutingPolicies().get(_calledPolicyName);
    if (policy == null) {
      environment.setError(true);
      return Result.builder().setBooleanValue(false).build();
    }
    boolean oldCallStatementContext = environment.getCallStatementContext();
    environment.setCallStatementContext(true);
    Result policyResult = policy.call(environment);
    environment.setCallStatementContext(oldCallStatementContext);
    return policyResult.toBuilder().setReturn(false).build();
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
}
