package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class PolicyMapMatchPolicyLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private final PolicyMap _policy;

   public PolicyMapMatchPolicyLine(PolicyMap policy) {
      _policy = policy;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public PolicyMap getPolicy() {
      return _policy;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.POLICY;
   }

}
