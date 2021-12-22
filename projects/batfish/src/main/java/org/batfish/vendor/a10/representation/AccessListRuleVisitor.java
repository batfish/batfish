package org.batfish.vendor.a10.representation;

/** A visitor of {@link AccessListRule} that returns a generic value. */
public interface AccessListRuleVisitor<T> {
  default T visit(AccessListRule rule) {
    return rule.accept(this);
  }

  T visitIcmp(AccessListRuleIcmp rule);

  T visitIp(AccessListRuleIp rule);

  T visitTcp(AccessListRuleTcp rule);

  T visitUdp(AccessListRuleUdp rule);
}
