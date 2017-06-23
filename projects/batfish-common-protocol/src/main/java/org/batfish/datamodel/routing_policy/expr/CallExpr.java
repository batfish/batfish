package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallExpr extends BooleanExpr {

   private static final String CALLED_POLICY_NAME_VAR = "calledPolicyName";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _calledPolicyName;

   @JsonCreator
   private CallExpr() {
   }

   public CallExpr(String includedPolicyName) {
      _calledPolicyName = includedPolicyName;
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
      }
      else if (!_calledPolicyName.equals(other._calledPolicyName)) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      RoutingPolicy policy = environment.getConfiguration().getRoutingPolicies()
            .get(_calledPolicyName);
      Result result;
      if (policy == null) {
         result = new Result();
         environment.setError(true);
         result.setBooleanValue(false);
      }
      else {
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

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public String getCalledPolicyName() {
      return _calledPolicyName;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_calledPolicyName == null) ? 0 : _calledPolicyName.hashCode());
      return result;
   }

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public void setCalledPolicyName(String calledPolicyName) {
      _calledPolicyName = calledPolicyName;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "<" + _calledPolicyName + ">";
   }

}
