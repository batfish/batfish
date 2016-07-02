package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallStatement extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String CALLED_POLICY_NAME_VAR = "calledPolicyName";

   private String _calledPolicyName;

   @JsonCreator
   public CallStatement() {
   }

   public CallStatement(String includedPolicyName) {
      _calledPolicyName = includedPolicyName;
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
