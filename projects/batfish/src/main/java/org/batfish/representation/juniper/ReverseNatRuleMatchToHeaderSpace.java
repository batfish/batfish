package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.SubRange;

public class ReverseNatRuleMatchToHeaderSpace implements NatRuleMatchVisitor<Void> {
  private static final String GLOBAL_ADDRESS_BOOK_PREFIX = "global~";

  private HeaderSpace.Builder _headerSpace;

  private ReverseNatRuleMatchToHeaderSpace() {
    _headerSpace = HeaderSpace.builder();
  }

  public static HeaderSpace toHeaderSpace(List<NatRuleMatch> matches) {
    ReverseNatRuleMatchToHeaderSpace v = new ReverseNatRuleMatchToHeaderSpace();
    matches.forEach(m -> m.accept(v));
    return v._headerSpace.build();
  }

  @Override
  public Void visitNatRuleMatchDstAddr(NatRuleMatchDstAddr natRuleMatchDstAddr) {
    _headerSpace.setSrcIps(natRuleMatchDstAddr.getPrefix().toIpSpace());
    return null;
  }

  @Override
  public Void visitNatRuleMatchDstAddrName(NatRuleMatchDstAddrName natRuleMatchDstAddrName) {
    _headerSpace.setSrcIps(
        new IpSpaceReference(GLOBAL_ADDRESS_BOOK_PREFIX + natRuleMatchDstAddrName.getName()));
    return null;
  }

  @Override
  public Void visitNatRuleMatchDstPort(NatRuleMatchDstPort natRuleMatchDstPort) {
    _headerSpace.setSrcPorts(
        ImmutableList.of(new SubRange(natRuleMatchDstPort.getFrom(), natRuleMatchDstPort.getTo())));
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcAddr(NatRuleMatchSrcAddr natRuleMatchSrcAddr) {
    _headerSpace.setDstIps(natRuleMatchSrcAddr.getPrefix().toIpSpace());
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcAddrName(NatRuleMatchSrcAddrName natRuleMatchSrcAddrName) {
    _headerSpace.setDstIps(
        new IpSpaceReference(GLOBAL_ADDRESS_BOOK_PREFIX + natRuleMatchSrcAddrName.getName()));
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort) {
    _headerSpace.setDstPorts(
        ImmutableList.of(new SubRange(natRuleMatchSrcPort.getFrom(), natRuleMatchSrcPort.getTo())));
    return null;
  }
}
