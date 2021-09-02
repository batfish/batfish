package org.batfish.vendor.check_point_management;

public interface NatRuleOrSectionVisitor<T> {
  default T visit(NatRuleOrSection natRuleOrSection) {
    return natRuleOrSection.accept(this);
  }

  T visitNatRule(NatRule natRule);

  T visitNatSection(NatSection natSection);
}
