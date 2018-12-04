package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.SubRange;

public class NatRuleMatchToHeaderSpace implements NatRuleMatchVisitor<Void> {
  private HeaderSpace.Builder _headerSpace;

  private NatRuleMatchToHeaderSpace() {
    _headerSpace = HeaderSpace.builder();
  }

  public static HeaderSpace toHeaderSpace(List<NatRuleMatch> matches) {
    NatRuleMatchToHeaderSpace v = new NatRuleMatchToHeaderSpace();
    matches.forEach(m -> m.accept(v));
    return v._headerSpace.build();
  }

  @Override
  public Void visitNatRuleMatchDstAddr(NatRuleMatchDstAddr natRuleMatchDstAddr) {
    _headerSpace.setDstIps(natRuleMatchDstAddr.getPrefix().toIpSpace());
    return null;
  }

  @Override
  public Void visitNatRuleMatchDstAddrName(NatRuleMatchDstAddrName natRuleMatchDstAddrName) {
    _headerSpace.setDstIps(new IpSpaceReference(natRuleMatchDstAddrName.getName()));
    return null;
  }

  @Override
  public Void visitNatRuleMatchDstPort(NatRuleMatchDstPort natRuleMatchDstPort) {
    _headerSpace.setDstPorts(
        ImmutableList.of(new SubRange(natRuleMatchDstPort.getFrom(), natRuleMatchDstPort.getTo())));
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcAddr(NatRuleMatchSrcAddr natRuleMatchSrcAddr) {
    _headerSpace.setSrcIps(natRuleMatchSrcAddr.getPrefix().toIpSpace());
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcAddrName(NatRuleMatchSrcAddrName natRuleMatchSrcAddrName) {
    _headerSpace.setSrcIps(new IpSpaceReference(natRuleMatchSrcAddrName.getName()));
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort) {
    _headerSpace.setSrcPorts(
        ImmutableList.of(new SubRange(natRuleMatchSrcPort.getFrom(), natRuleMatchSrcPort.getTo())));
    return null;
  }
}
