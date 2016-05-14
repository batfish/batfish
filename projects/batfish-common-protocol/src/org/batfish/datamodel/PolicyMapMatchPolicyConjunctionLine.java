package org.batfish.datamodel;

import java.util.Set;

public final class PolicyMapMatchPolicyConjunctionLine extends
      PolicyMapMatchLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<PolicyMap> _conjuncts;

   public PolicyMapMatchPolicyConjunctionLine(Set<PolicyMap> conjuncts) {
      _conjuncts = conjuncts;
   }

   public Set<PolicyMap> getConjuncts() {
      return _conjuncts;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.POLICY_CONJUNCTION;
   }

}
