package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallStatement extends Statement {

   private static final String CALLED_POLICY_NAME_VAR = "calledPolicyName";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _calledPolicyName;

   @JsonCreator
   private CallStatement() {
   }

   public CallStatement(String includedPolicyName) {
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
      CallStatement other = (CallStatement) obj;
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
   public Result execute(Environment environment) {
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
         result = policy.call(environment);
         result.setReturn(false);
         environment.setCallStatementContext(oldCallStatementContext);
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

}
