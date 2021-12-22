package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.TraceElements.traceElementForDestAddressAny;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForDestHost;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForProtocol;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForProtocolPortRange;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForSourceAddressAny;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForSourceHost;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Convert an {@link AccessListRule} to its corresponding {@link AclLineMatchExpr}. */
public final class AccessListRuleToMatchExpr implements AccessListRuleVisitor<AclLineMatchExpr> {
  static final AccessListRuleToMatchExpr INSTANCE = new AccessListRuleToMatchExpr();

  @Override
  public AclLineMatchExpr visitIcmp(AccessListRuleIcmp rule) {
    return buildFinalExpr(rule, protocolToMatchExpr(IpProtocol.ICMP));
  }

  @Override
  public AclLineMatchExpr visitIp(AccessListRuleIp rule) {
    return buildFinalExpr(rule, null);
  }

  @Override
  public AclLineMatchExpr visitTcp(AccessListRuleTcp rule) {
    if (rule.getDestinationRange() != null) {
      return buildFinalExpr(rule, portRangeToMatchExpr(IpProtocol.TCP, rule.getDestinationRange()));
    }
    return buildFinalExpr(rule, protocolToMatchExpr(IpProtocol.TCP));
  }

  @Override
  public AclLineMatchExpr visitUdp(AccessListRuleUdp rule) {
    if (rule.getDestinationRange() != null) {
      return buildFinalExpr(rule, portRangeToMatchExpr(IpProtocol.UDP, rule.getDestinationRange()));
    }
    return buildFinalExpr(rule, protocolToMatchExpr(IpProtocol.UDP));
  }

  /**
   * Helper to build final {@link AclLineMatchExpr} for the generic {@link AccessListRule} given its
   * rule-type-specific {@code specificExpr}.
   */
  private AclLineMatchExpr buildFinalExpr(
      AccessListRule rule, @Nullable AclLineMatchExpr specificExpr) {
    ImmutableList.Builder<AclLineMatchExpr> exprs =
        ImmutableList.<AclLineMatchExpr>builder()
            .add(AccessListAddressToMatchExpr.visitDst(rule.getDestination()))
            .add(AccessListAddressToMatchExpr.visitSrc(rule.getSource()));
    if (specificExpr != null) {
      exprs.add(specificExpr);
    }
    return AclLineMatchExprs.and(exprs.build());
  }

  /** Helper to generate a match expression for a port range of a given {@link IpProtocol}. */
  private AclLineMatchExpr portRangeToMatchExpr(IpProtocol protocol, SubRange range) {
    return AclLineMatchExprs.and(
        traceElementForProtocolPortRange(protocol, range),
        AclLineMatchExprs.matchIpProtocol(protocol),
        AclLineMatchExprs.matchDstPort(IntegerSpace.of(range)));
  }

  /** Helper to generate a match expression for a given {@link IpProtocol}. */
  private AclLineMatchExpr protocolToMatchExpr(IpProtocol protocol) {
    return AclLineMatchExprs.matchIpProtocol(protocol, traceElementForProtocol(protocol));
  }

  /** Convert an {@link AccessListAddress} to a corresponding {@link AclLineMatchExpr}. */
  private static final class AccessListAddressToMatchExpr {
    static final AccessListAddressToMatchExprImpl _source =
        new AccessListAddressToMatchExprImpl(true);
    static final AccessListAddressToMatchExprImpl _dest =
        new AccessListAddressToMatchExprImpl(false);

    private static final class AccessListAddressToMatchExprImpl
        implements AccessListAddressVisitor<AclLineMatchExpr> {
      @Override
      public AclLineMatchExpr visitAny(AccessListAddressAny address) {
        IpSpace ip = UniverseIpSpace.INSTANCE;
        return _isSource
            ? AclLineMatchExprs.matchSrc(ip, traceElementForSourceAddressAny())
            : AclLineMatchExprs.matchDst(ip, traceElementForDestAddressAny());
      }

      @Override
      public AclLineMatchExpr visitHost(AccessListAddressHost host) {
        IpSpace ip = host.getHost().toIpSpace();
        return _isSource
            ? AclLineMatchExprs.matchSrc(ip, traceElementForSourceHost(host))
            : AclLineMatchExprs.matchDst(ip, traceElementForDestHost(host));
      }

      public AccessListAddressToMatchExprImpl(boolean isSource) {
        _isSource = isSource;
      }

      private final boolean _isSource;
    }

    public static AclLineMatchExpr visitSrc(AccessListAddress address) {
      return _source.visit(address);
    }

    public static AclLineMatchExpr visitDst(AccessListAddress address) {
      // TODO confirm dest address matching behavior
      // Docs aren't clear if this matches original or translated destination address
      return _dest.visit(address);
    }
  }
}
