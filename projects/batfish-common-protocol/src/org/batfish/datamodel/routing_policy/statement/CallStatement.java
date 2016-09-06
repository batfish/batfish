package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallStatement extends AbstractStatement {

   private static final String CALLED_POLICY_NAME_VAR = "calledPolicyName";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _calledPolicyName;

   @JsonCreator
   public CallStatement() {
   }

   public CallStatement(String includedPolicyName) {
      _calledPolicyName = includedPolicyName;
   }

   @Override
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
      RoutingPolicy policy = environment.getConfiguration().getRoutingPolicies()
            .get(_calledPolicyName);
      Result result;
      if (policy == null) {
         result = new Result();
         environment.setError(true);
         result.setBooleanValue(false);
      }
      else {
         boolean oldCallStatementContext = environment
               .getCallStatementContext();
         environment.setCallStatementContext(true);
         result = policy.call(environment, route);
         environment.setCallStatementContext(oldCallStatementContext);
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
