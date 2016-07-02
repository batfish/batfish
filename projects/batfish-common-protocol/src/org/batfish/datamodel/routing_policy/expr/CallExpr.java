package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallExpr implements BooleanExpr {

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

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public String getCalledPolicyName() {
      return _calledPolicyName;
   }

   @JsonProperty(CALLED_POLICY_NAME_VAR)
   public void setCalledPolicyName(String calledPolicyName) {
      _calledPolicyName = calledPolicyName;
   }

}
