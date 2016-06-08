package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchPolicyLine extends PolicyMapMatchLine {

   private static final String POLICY_VAR = "policy";

   private static final long serialVersionUID = 1L;

   private PolicyMap _policy;

   @JsonCreator
   public PolicyMapMatchPolicyLine() {
   }

   public PolicyMapMatchPolicyLine(PolicyMap policy) {
      _policy = policy;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(POLICY_VAR)
   public PolicyMap getPolicy() {
      return _policy;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.POLICY;
   }

   @JsonProperty(POLICY_VAR)
   public void setPolicy(PolicyMap policy) {
      _policy = policy;
   }

}
