package org.batfish.representation.palo_alto;

/** A visitor of {@link PolicyRuleMatch}. */
public interface PolicyRuleMatchVisitor<T> {

  T visitPolicyRuleMatchAddressPrefixSet(
      PolicyRuleMatchAddressPrefixSet policyRuleMatchAddressPrefixSet);

  T visitPolicyRuleMatchFromPeerSet(PolicyRuleMatchFromPeerSet policyRuleMatchFromPeerSet);
}
