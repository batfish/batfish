package org.batfish.vendor.check_point_management;

/** Indicates that a rule should be applied to all installation targets of the package. */
public final class PolicyTargets extends Global {

  @Override
  public <T> T accept(NatTranslatedServiceVisitor<T> visitor) {
    return visitor.visitPolicyTargets(this);
  }

  @Override
  public <T> T accept(NatTranslatedSrcOrDstVisitor<T> visitor) {
    return visitor.visitPolicyTargets(this);
  }

  PolicyTargets(Uid uid) {
    super(NAME_POLICY_TARGETS, uid);
  }
}
