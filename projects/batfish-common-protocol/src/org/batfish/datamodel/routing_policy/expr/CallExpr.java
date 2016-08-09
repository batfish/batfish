package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallExpr extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String CALLED_POLICY_NAME_VAR = "calledPolicyName";

   private String _calledPolicyName;

   @JsonCreator
   public CallExpr() {
   }

   public CallExpr(String includedPolicyName) {
      _calledPolicyName = includedPolicyName;
   }

   @Override
   public Result evaluate(Environment environment, Route route) {
      RoutingPolicy policy = environment.getConfiguration()
            .getRoutingPolicies().get(_calledPolicyName);
      Result result;
      if (policy == null) {
         result = new Result();
         environment.setError(true);
         result.setBooleanValue(false);
      }
      else {
         boolean oldCallExprContext = environment.getCallExprContext();
         environment.setCallExprContext(true);
         result = policy.call(environment, route);
         environment.setCallExprContext(oldCallExprContext);
      }
      return result;
   }

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public String getCalledPolicyName() {
      return _calledPolicyName;
   }

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public void setCalledPolicyName(String calledPolicyName) {
      _calledPolicyName = calledPolicyName;
   }

}
