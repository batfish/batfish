package org.batfish.representation.palo_alto;

/** A visitor of {@link PolicyRuleUpdate}. */
public interface PolicyRuleUpdateVisitior<T> {

  T visitPolicyRuleUpdateMetric(PolicyRuleUpdateMetric policyRuleUpdateMetric);

  T visitPolicyRuleUpdateOrigin(PolicyRuleUpdateOrigin policyRuleUpdateOrigin);

  T visitPolicyRuleUpdateWeight(PolicyRuleUpdateWeight policyRuleUpdateWeight);
}
