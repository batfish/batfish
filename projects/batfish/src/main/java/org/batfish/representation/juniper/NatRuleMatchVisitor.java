package org.batfish.representation.juniper;

public interface NatRuleMatchVisitor<T> {
  T visitNatRuleMatchDstAddr(NatRuleMatchDstAddr natRuleMatchDstAddr);

  T visitNatRuleMatchDstAddrName(NatRuleMatchDstAddrName natRuleMatchDstAddrName);

  T visitNatRuleMatchDstPort(NatRuleMatchDstPort natRuleMatchDstPort);

  T visitNatRuleMatchSrcAddr(NatRuleMatchSrcAddr natRuleMatchSrcAddr);

  T visitNatRuleMatchSrcAddrName(NatRuleMatchSrcAddrName natRuleMatchSrcAddrName);

  T visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort);
}
