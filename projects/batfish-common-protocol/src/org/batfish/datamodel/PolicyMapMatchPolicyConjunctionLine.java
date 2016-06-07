package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PolicyMapMatchPolicyConjunctionLine extends
      PolicyMapMatchLine {

   private static final String CONJUNCTS_VAR = "conjuncts";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<PolicyMap> _conjuncts;

   @JsonCreator
   public PolicyMapMatchPolicyConjunctionLine(
         @JsonProperty(CONJUNCTS_VAR) Set<PolicyMap> conjuncts) {
      _conjuncts = conjuncts;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(CONJUNCTS_VAR)
   public Set<PolicyMap> getConjuncts() {
      return _conjuncts;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.POLICY_CONJUNCTION;
   }

}
