package org.batfish.datamodel.acl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.DscpType;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.UniverseIpSpace;

public final class AclLineMatchExprs {

  private AclLineMatchExprs() {}

  public static final FalseExpr FALSE = FalseExpr.INSTANCE;

  public static final OriginatingFromDevice ORIGINATING_FROM_DEVICE =
      OriginatingFromDevice.INSTANCE;

  public static final TrueExpr TRUE = TrueExpr.INSTANCE;

  static final AclLineMatchExpr ICMP_FLOWS = matchIpProtocol(IpProtocol.ICMP);
  static final AclLineMatchExpr TCP_FLOWS = matchIpProtocol(IpProtocol.TCP);
  static final AclLineMatchExpr UDP_FLOWS = matchIpProtocol(IpProtocol.UDP);

  /**
   * TCP-specific settings for the first packet of a TCP flow.
   *
   * <p>Note this <strong>DOES NOT</strong> handle non-TCP fields like fragment offset.
   *
   * @see #NEW_FLOWS for other fields.
   */
  @VisibleForTesting
  static final AclLineMatchExpr NEW_TCP_FLOWS =
      and(TCP_FLOWS, matchTcpFlags(TcpFlagsMatchConditions.SYN_ONLY_TCP_FLAG));

  /** A reusable expression to indicate new flows. */
  public static final AclLineMatchExpr NEW_FLOWS =
      and(
          // A new flow is the first fragment or is not fragmented
          matchFragmentOffset(0),
          // A new TCP flow has specific TCP flags set
          implies(TCP_FLOWS, NEW_TCP_FLOWS));

  /** A reusable expression to indicate flows that meet basic integrity constraints. */
  public static final AclLineMatchExpr VALID_FLOWS = makeValidFlows();

  private static AclLineMatchExpr makeValidFlows() {
    // Minimum IP packet size is 20 bytes.
    AclLineMatchExpr ip = not(matchPacketLength(IntegerSpace.of(Range.closedOpen(0, 20))));
    AclLineMatchExpr icmp =
        implies(
            ICMP_FLOWS,
            // Minimum ICMP packet size is 64 bytes.
            not(matchPacketLength(IntegerSpace.of(Range.closedOpen(0, 64)))));
    AclLineMatchExpr tcp =
        implies(
            TCP_FLOWS,
            and(
                // Minimum TCP packet size is 40 bytes.
                not(matchPacketLength(IntegerSpace.of(Range.closedOpen(0, 40)))),
                // Ports cannot be 0.
                not(matchSrcPort(0)),
                not(matchDstPort(0))));
    AclLineMatchExpr udp =
        implies(
            UDP_FLOWS,
            and(
                // Minimum UDP packet size is 28 bytes.
                not(matchPacketLength(IntegerSpace.of(Range.closedOpen(0, 28)))),
                // Ports cannot be 0.
                not(matchSrcPort(0)),
                not(matchDstPort(0))));
    return and(ip, icmp, tcp, udp);
  }

  public static AclLineMatchExpr and(String traceElement, AclLineMatchExpr... exprs) {
    return and(Arrays.asList(exprs), TraceElement.of(traceElement));
  }

  public static AclLineMatchExpr and(TraceElement traceElement, AclLineMatchExpr... exprs) {
    return and(Arrays.asList(exprs), traceElement);
  }

  public static AclLineMatchExpr and(AclLineMatchExpr... exprs) {
    return and(Arrays.asList(exprs));
  }

  public static AclLineMatchExpr and(Collection<AclLineMatchExpr> exprs) {
    return and(exprs, null);
  }

  /**
   * Constant-time constructor for AndMatchExpr. Simplifies if given zero or one conjunct. Doesn't
   * do other simplifications that require more work (like removing all {@link TrueExpr TrueExprs}).
   */
  public static AclLineMatchExpr and(
      Collection<AclLineMatchExpr> exprs, TraceElement traceElement) {
    if (exprs.isEmpty()) {
      return traceElement == null ? TRUE : new TrueExpr(traceElement);
    } else if (traceElement == null && exprs.size() == 1) {
      return Iterables.getOnlyElement(exprs);
    }
    return new AndMatchExpr(exprs, traceElement);
  }

  /**
   * Construct an {@link AclLineMatchExpr} encoding if {@code condition} then {@code consequent}.
   */
  public static AclLineMatchExpr implies(AclLineMatchExpr condition, AclLineMatchExpr consequent) {
    return or(not(condition), consequent);
  }

  public static MatchHeaderSpace match(HeaderSpace headerSpace) {
    return new MatchHeaderSpace(headerSpace);
  }

  public static MatchHeaderSpace match(HeaderSpace headerSpace, TraceElement traceElement) {
    return new MatchHeaderSpace(headerSpace, traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchDscp(DscpType dscp) {
    return matchDscp(dscp.number(), null);
  }

  public static @Nonnull AclLineMatchExpr matchDscp(int dscp) {
    return matchDscp(dscp, null);
  }

  public static @Nonnull AclLineMatchExpr matchDscp(int dscp, @Nullable TraceElement traceElement) {
    checkArgument(0 <= dscp && dscp <= 63, "Invalid DSCP: %s", dscp);
    return new MatchHeaderSpace(
        HeaderSpace.builder().setDscps(ImmutableList.of(dscp)).build(), traceElement);
  }

  public static AclLineMatchExpr matchDst(IpSpace ipSpace) {
    return matchDst(ipSpace, null);
  }

  public static AclLineMatchExpr matchDst(IpSpace ipSpace, @Nullable TraceElement traceElement) {
    if (ipSpace.equals(UniverseIpSpace.INSTANCE)) {
      return traceElement == null ? TRUE : new TrueExpr(traceElement);
    }
    return new MatchHeaderSpace(HeaderSpace.builder().setDstIps(ipSpace).build(), traceElement);
  }

  public static AclLineMatchExpr matchDst(Ip ip) {
    return matchDst(ip.toIpSpace());
  }

  public static AclLineMatchExpr matchDst(IpWildcard wc) {
    return matchDst(wc.toIpSpace());
  }

  public static AclLineMatchExpr matchDst(Prefix prefix) {
    return matchDst(prefix.toIpSpace());
  }

  public static AclLineMatchExpr matchDstIp(String ip) {
    return matchDst(Ip.parse(ip).toIpSpace());
  }

  public static @Nonnull AclLineMatchExpr matchDstPort(int port) {
    checkArgument(0 <= port && port <= 0xFFFF, "Invalid port: %s", port);
    return match(
        HeaderSpace.builder().setDstPorts(ImmutableList.of(SubRange.singleton(port))).build());
  }

  public static @Nonnull AclLineMatchExpr matchDstPort(IntegerSpace portSpace) {
    return matchDstPort(portSpace, null);
  }

  public static @Nonnull AclLineMatchExpr matchDstPort(
      IntegerSpace portSpace, @Nullable TraceElement traceElement) {
    checkArgument(
        0 <= portSpace.least() && portSpace.greatest() <= 0xFFFF,
        "Invalid port space: %s",
        portSpace);
    return match(HeaderSpace.builder().setDstPorts(portSpace.getSubRanges()).build(), traceElement);
  }

  public static AclLineMatchExpr matchDstPrefix(String prefix) {
    return matchDst(Prefix.parse(prefix));
  }

  public static AclLineMatchExpr matchSrc(IpSpace ipSpace) {
    return matchSrc(ipSpace, null);
  }

  public static AclLineMatchExpr matchSrc(IpSpace ipSpace, @Nullable TraceElement traceElement) {
    if (ipSpace.equals(UniverseIpSpace.INSTANCE)) {
      return traceElement == null ? TRUE : new TrueExpr(traceElement);
    }
    return new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(ipSpace).build(), traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchFragmentOffset(int fragmentOffset) {
    checkArgument(
        0 <= fragmentOffset && fragmentOffset <= 8191,
        "Invalid fragment offset: %s",
        fragmentOffset);
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setFragmentOffsets(ImmutableList.of(SubRange.singleton(fragmentOffset)))
            .build());
  }

  public static @Nonnull AclLineMatchExpr matchFragmentOffset(IntegerSpace fragmentOffsetSpace) {
    checkArgument(
        0 <= fragmentOffsetSpace.least() && fragmentOffsetSpace.greatest() <= 8191,
        "Invalid fragmentOffsetSpace: %s",
        fragmentOffsetSpace);
    return new MatchHeaderSpace(
        HeaderSpace.builder().setFragmentOffsets(fragmentOffsetSpace.getSubRanges()).build());
  }

  public static @Nonnull AclLineMatchExpr matchIcmp(int icmpType, int icmpCode) {
    return and(matchIcmpType(icmpType), matchIcmpCode(icmpCode));
  }

  public static @Nonnull AclLineMatchExpr matchIcmp(IcmpCode code) {
    return and(matchIcmpType(code.getType()), matchIcmpCode(code.getCode()));
  }

  public static @Nonnull AclLineMatchExpr matchIcmpCode(int icmpCode) {
    return matchIcmpCode(icmpCode, null);
  }

  public static @Nonnull AclLineMatchExpr matchIcmpCode(
      int icmpCode, @Nullable TraceElement traceElement) {
    checkArgument(0 <= icmpCode && icmpCode <= 255, "Invalid ICMP code: %s", icmpCode);
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIcmpCodes(ImmutableList.of(SubRange.singleton(icmpCode))).build(),
        traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchIcmpType(int icmpType) {
    return matchIcmpType(icmpType, null);
  }

  public static @Nonnull AclLineMatchExpr matchIcmpType(
      int icmpType, @Nullable TraceElement traceElement) {
    checkArgument(0 <= icmpType && icmpType <= 255, "Invalid ICMP type: %s", icmpType);
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIcmpTypes(ImmutableList.of(SubRange.singleton(icmpType))).build(),
        traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchIpProtocol(int ipProtocolNumber) {
    checkArgument(
        0 <= ipProtocolNumber && ipProtocolNumber <= 255,
        "Invalid IP protocol number: %s",
        ipProtocolNumber);
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.fromNumber(ipProtocolNumber)))
            .build());
  }

  public static @Nonnull AclLineMatchExpr matchIpProtocol(IpProtocol ipProtocol) {
    return matchIpProtocol(ipProtocol, null);
  }

  public static @Nonnull AclLineMatchExpr matchIpProtocol(
      IpProtocol ipProtocol, @Nullable TraceElement traceElement) {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIpProtocols(ImmutableList.of(ipProtocol)).build(), traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchPacketLength(IntegerSpace packetLengthSpace) {
    checkArgument(
        0 <= packetLengthSpace.least() && packetLengthSpace.greatest() <= 0xFFFF,
        "Invalid packetLengthSpace: %s",
        packetLengthSpace);
    return new MatchHeaderSpace(
        HeaderSpace.builder().setPacketLengths(packetLengthSpace.getSubRanges()).build());
  }

  public static @Nonnull AclLineMatchExpr matchPacketLength(int packetLength) {
    checkArgument(
        0 <= packetLength && packetLength <= 0xFFFF, "Invalid packetLength: %s", packetLength);
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setPacketLengths(ImmutableList.of(SubRange.singleton(packetLength)))
            .build());
  }

  public static AclLineMatchExpr matchSrc(Ip ip) {
    return matchSrc(ip.toIpSpace());
  }

  public static @Nonnull AclLineMatchExpr matchSrc(IpWildcard wc) {
    return matchSrc(wc.toIpSpace());
  }

  public static AclLineMatchExpr matchSrc(Prefix prefix) {
    return matchSrc(prefix.toIpSpace());
  }

  public static AclLineMatchExpr matchSrcIp(String ip) {
    return matchSrc(Ip.parse(ip).toIpSpace());
  }

  public static AclLineMatchExpr matchSrcPrefix(String prefix) {
    return matchSrc(Prefix.parse(prefix).toIpSpace());
  }

  public static MatchHeaderSpace matchSrcPort(int port) {
    checkArgument(0 <= port && port <= 0xFFFF, "Invalid port: %s", port);
    return match(
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(port))).build());
  }

  public static @Nonnull AclLineMatchExpr matchSrcPort(IntegerSpace portSpace) {
    checkArgument(
        0 <= portSpace.least() && portSpace.greatest() <= 0xFFFF,
        "Invalid port space: %s",
        portSpace);
    return match(HeaderSpace.builder().setSrcPorts(portSpace.getSubRanges()).build());
  }

  public static @Nonnull MatchSrcInterface matchSrcInterface(Iterable<String> ifaces) {
    return new MatchSrcInterface(ImmutableList.copyOf(ifaces));
  }

  public static @Nonnull MatchSrcInterface matchSrcInterface(String... ifaces) {
    return matchSrcInterface(ImmutableList.copyOf(ifaces));
  }

  public static @Nonnull MatchSrcInterface matchSrcInterface(
      TraceElement traceElement, String... ifaces) {
    return new MatchSrcInterface(ImmutableList.copyOf(ifaces), traceElement);
  }

  public static @Nonnull AclLineMatchExpr matchTcpFlags(
      TcpFlagsMatchConditions... tcpFlagsMatchConditions) {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setTcpFlags(ImmutableList.copyOf(tcpFlagsMatchConditions)).build());
  }

  public static NotMatchExpr not(String traceElement, AclLineMatchExpr expr) {
    return new NotMatchExpr(expr, TraceElement.of(traceElement));
  }

  /**
   * Smart constructor for {@link NotMatchExpr} that does constant-time simplifications (i.e. when
   * expr is {@link #TRUE} or {@link #FALSE}).
   */
  public static AclLineMatchExpr not(AclLineMatchExpr expr) {
    if (expr == TRUE) {
      return FALSE;
    }
    if (expr == FALSE) {
      return TRUE;
    }
    if (expr instanceof NotMatchExpr) {
      return ((NotMatchExpr) expr).getOperand();
    }
    return new NotMatchExpr(expr);
  }

  public static AclLineMatchExpr or(AclLineMatchExpr... exprs) {
    return or(Arrays.asList(exprs));
  }

  public static AclLineMatchExpr or(String traceElement, AclLineMatchExpr... exprs) {
    return or(TraceElement.of(traceElement), exprs);
  }

  public static AclLineMatchExpr or(TraceElement traceElement, AclLineMatchExpr... exprs) {
    return or(Arrays.asList(exprs), traceElement);
  }

  public static AclLineMatchExpr or(Collection<AclLineMatchExpr> exprs) {
    return or(exprs, null);
  }

  /**
   * Constant-time constructor for OrMatchExpr. Simplifies if given zero or one conjunct. Doesn't do
   * other simplifications that require more work (like removing all {@link FalseExpr FalseExprs}).
   */
  public static AclLineMatchExpr or(
      Collection<AclLineMatchExpr> exprs, @Nullable TraceElement traceElement) {
    if (exprs.isEmpty()) {
      return traceElement == null ? FALSE : new FalseExpr(traceElement);
    } else if (traceElement == null && exprs.size() == 1) {
      return Iterables.getOnlyElement(exprs);
    }
    return new OrMatchExpr(exprs, traceElement);
  }

  public static PermittedByAcl permittedByAcl(String aclName) {
    return new PermittedByAcl(aclName);
  }

  public static PermittedByAcl permittedByAcl(String aclName, @Nullable TraceElement traceElement) {
    return new PermittedByAcl(aclName, traceElement);
  }

  public static DeniedByAcl deniedByAcl(String aclName) {
    return new DeniedByAcl(aclName);
  }

  public static DeniedByAcl deniedByAcl(String aclName, @Nullable TraceElement traceElement) {
    return new DeniedByAcl(aclName, traceElement);
  }
}
