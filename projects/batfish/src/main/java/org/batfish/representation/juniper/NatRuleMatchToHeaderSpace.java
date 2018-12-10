package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.SubRange;

public class NatRuleMatchToHeaderSpace implements NatRuleMatchVisitor<Void> {
  /* From the Juniper docs:
   *   NAT rules can use address objects only from the global address book.
   *   https://www.juniper.net/documentation/en_US/junos/topics/topic-map/
   *     security-address-books-sets.html#id-understanding-global-address-books
   */
  private static final String GLOBAL_ADDRESS_BOOK_PREFIX = "global~";

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
    _headerSpace.setDstIps(
        new IpSpaceReference(GLOBAL_ADDRESS_BOOK_PREFIX + natRuleMatchDstAddrName.getName()));
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
    _headerSpace.setSrcIps(
        new IpSpaceReference(GLOBAL_ADDRESS_BOOK_PREFIX + natRuleMatchSrcAddrName.getName()));
    return null;
  }

  @Override
  public Void visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort) {
    _headerSpace.setSrcPorts(
        ImmutableList.of(new SubRange(natRuleMatchSrcPort.getFrom(), natRuleMatchSrcPort.getTo())));
    return null;
  }
}
