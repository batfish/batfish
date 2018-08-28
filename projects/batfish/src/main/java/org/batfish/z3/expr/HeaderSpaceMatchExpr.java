package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.FlowState;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.z3.Field;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;
import org.batfish.z3.expr.visitors.IpSpaceBooleanExprTransformer;

public final class HeaderSpaceMatchExpr extends BooleanExpr {

  public static BooleanExpr matchDscp(Set<Integer> dscps) {
    return new OrExpr(
        dscps
            .stream()
            .map(
                dscp ->
                    new EqExpr(
                        new VarIntExpr(Field.DSCP), new LitIntExpr(dscp, Field.DSCP.getSize())))
            .collect(ImmutableList.toImmutableList()));
  }

  public BooleanExpr matchDstIp(IpSpace ipSpace) {
    return matchIpSpace(ipSpace, Field.DST_IP);
  }

  public static BooleanExpr matchDstIp(IpSpace ipSpace, Map<String, IpSpace> namedIpSpaces) {
    return matchIpSpace(ipSpace, Field.DST_IP, namedIpSpaces);
  }

  public static BooleanExpr matchDstPort(Set<SubRange> dstPortRanges) {
    return RangeMatchExpr.fromSubRanges(Field.DST_PORT, Field.DST_PORT.getSize(), dstPortRanges);
  }

  public static BooleanExpr matchDstProtocol(Set<Protocol> dstProtocols) {
    return matchProtocol(dstProtocols, false, true);
  }

  public static BooleanExpr matchEcn(Set<Integer> dscps) {
    return new OrExpr(
        dscps
            .stream()
            .map(
                dscp ->
                    new EqExpr(
                        new VarIntExpr(Field.ECN), new LitIntExpr(dscp, Field.ECN.getSize())))
            .collect(ImmutableList.toImmutableList()));
  }

  public static BooleanExpr matchFragmentOffset(Set<SubRange> fragmentOffsetRanges) {
    return RangeMatchExpr.fromSubRanges(
        Field.FRAGMENT_OFFSET, Field.FRAGMENT_OFFSET.getSize(), fragmentOffsetRanges);
  }

  public static BooleanExpr matchIcmpCode(Set<SubRange> icmpCodes) {
    return RangeMatchExpr.fromSubRanges(Field.ICMP_CODE, Field.ICMP_CODE.getSize(), icmpCodes);
  }

  public static BooleanExpr matchIcmpType(Set<SubRange> icmpTypes) {
    return RangeMatchExpr.fromSubRanges(Field.ICMP_TYPE, Field.ICMP_TYPE.getSize(), icmpTypes);
  }

  public BooleanExpr matchIpSpace(IpSpace ipSpace, Field ipField) {
    return ipSpace.accept(new IpSpaceBooleanExprTransformer(_namedIpSpaces, ipField));
  }

  public static BooleanExpr matchIpSpace(
      IpSpace ipSpace, Field ipField, Map<String, IpSpace> namedIpSpaces) {
    return ipSpace.accept(new IpSpaceBooleanExprTransformer(namedIpSpaces, ipField));
  }

  public static BooleanExpr matchIp(Ip ip, Field ipField) {
    assert ipField.getSize() == 32;
    return new EqExpr(new LitIntExpr(ip), new VarIntExpr(ipField));
  }

  public static BooleanExpr matchIpWildcards(Set<IpWildcard> ipWildcards, Field ipField) {
    return new OrExpr(
        ipWildcards
            .stream()
            .map(ipWildcard -> matchIpWildcard(ipWildcard, ipField))
            .collect(Collectors.toList()));
  }

  public static BooleanExpr matchPrefix(Prefix prefix, Field ipField) {
    long ip = prefix.getStartIp().asLong();
    int ipWildcardBits = Prefix.MAX_PREFIX_LENGTH - prefix.getPrefixLength();
    int ipStart = ipWildcardBits;
    int ipEnd = Prefix.MAX_PREFIX_LENGTH - 1;
    if (ipStart < Prefix.MAX_PREFIX_LENGTH) {
      IntExpr extractIp = ExtractExpr.newExtractExpr(ipField, ipStart, ipEnd);
      LitIntExpr ipMatchLit = new LitIntExpr(ip, ipStart, ipEnd);
      return new EqExpr(extractIp, ipMatchLit);
    } else {
      return TrueExpr.INSTANCE;
    }
  }

  public static BooleanExpr matchIpWildcard(IpWildcard ipWildcard, Field ipField) {
    if (ipWildcard.isPrefix()) {
      return matchPrefix(ipWildcard.toPrefix(), ipField);
    }

    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcard().asLong();
    ImmutableList.Builder<BooleanExpr> matchIp = ImmutableList.builder();
    for (int currentBitIndex = 0; currentBitIndex < Prefix.MAX_PREFIX_LENGTH; currentBitIndex++) {
      long mask = 1L << currentBitIndex;
      long currentWildcardBit = mask & wildcard;
      boolean useBit = currentWildcardBit == 0;
      if (useBit) {
        IntExpr extractIp = ExtractExpr.newExtractExpr(ipField, currentBitIndex, currentBitIndex);
        LitIntExpr srcIpMatchLit = new LitIntExpr(ip, currentBitIndex, currentBitIndex);
        EqExpr matchIpBit = new EqExpr(extractIp, srcIpMatchLit);
        matchIp.add(matchIpBit);
      }
    }
    return new AndExpr(matchIp.build());
  }

  public static BooleanExpr matchIpProtocol(Set<IpProtocol> ipProtocols) {
    return new OrExpr(
        ipProtocols
            .stream()
            .map(IpProtocol::number)
            .map(
                num ->
                    new EqExpr(
                        new VarIntExpr(Field.IP_PROTOCOL),
                        new LitIntExpr(num, Field.IP_PROTOCOL.getSize())))
            .collect(ImmutableList.toImmutableList()));
  }

  public BooleanExpr matchOrigSrcIp(IpSpace ipSpace) {
    return matchIpSpace(ipSpace, Field.ORIG_SRC_IP);
  }

  public static BooleanExpr matchPacketLength(Set<SubRange> packetLengths) {
    return RangeMatchExpr.fromSubRanges(
        Field.PACKET_LENGTH, Field.PACKET_LENGTH.getSize(), packetLengths);
  }

  public static BooleanExpr matchProtocol(Set<Protocol> protocols, boolean useSrc, boolean useDst) {
    OrExpr matchesSomeProtocol =
        new OrExpr(
            protocols
                .stream()
                .map(
                    protocol -> {
                      int protocolNumber = protocol.getIpProtocol().number();
                      Integer port = protocol.getPort();
                      VarIntExpr protocolVar = new VarIntExpr(Field.IP_PROTOCOL);
                      LitIntExpr protocolLit =
                          new LitIntExpr(protocolNumber, Field.IP_PROTOCOL.getSize());
                      EqExpr matchProtocol = new EqExpr(protocolVar, protocolLit);
                      ImmutableList.Builder<BooleanExpr> matchProtocolAndPort =
                          ImmutableList.builder();
                      matchProtocolAndPort.add(matchProtocol);
                      if (port != null) {

                        VarIntExpr dstPortVar = new VarIntExpr(Field.DST_PORT);
                        VarIntExpr srcPortVar = new VarIntExpr(Field.SRC_PORT);
                        LitIntExpr portLit = new LitIntExpr(port, Field.SRC_PORT.getSize());
                        EqExpr matchDstPort = new EqExpr(dstPortVar, portLit);
                        EqExpr matchSrcPort = new EqExpr(srcPortVar, portLit);
                        BooleanExpr matchSpecifiedPorts;
                        if (useSrc) {
                          if (useDst) {
                            matchSpecifiedPorts =
                                new OrExpr(ImmutableList.of(matchDstPort, matchSrcPort));
                          } else {
                            matchSpecifiedPorts = matchSrcPort;
                          }
                        } else if (useDst) {
                          matchSpecifiedPorts = matchDstPort;
                        } else {
                          throw new BatfishException("useSrc or useDst cannot both be false");
                        }
                        matchProtocolAndPort.add(matchSpecifiedPorts);
                      }
                      return new AndExpr(matchProtocolAndPort.build());
                    })
                .collect(ImmutableList.toImmutableList()));
    return matchesSomeProtocol;
  }

  public BooleanExpr matchSrcIp(IpSpace srcIpSpace) {
    return matchIpSpace(srcIpSpace, Field.SRC_IP);
  }

  public static BooleanExpr matchSrcIp(IpSpace srcIpSpace, Map<String, IpSpace> namedIpSpaces) {
    return matchIpSpace(srcIpSpace, Field.SRC_IP, namedIpSpaces);
  }

  public BooleanExpr matchSrcOrDstIp(IpSpace ipSpace) {
    return ipSpace.accept(
        new IpSpaceBooleanExprTransformer(_namedIpSpaces, Field.SRC_IP, Field.DST_IP));
  }

  public static BooleanExpr matchSrcOrDstPort(Set<SubRange> srcOrDstPorts) {
    return new OrExpr(ImmutableList.of(matchSrcPort(srcOrDstPorts), matchDstPort(srcOrDstPorts)));
  }

  public static BooleanExpr matchSrcOrDstProtocol(Set<Protocol> srcOrDstProtocols) {
    return matchProtocol(srcOrDstProtocols, true, true);
  }

  public static BooleanExpr matchSrcPort(Set<SubRange> srcPortRanges) {
    return RangeMatchExpr.fromSubRanges(Field.SRC_PORT, Field.SRC_PORT.getSize(), srcPortRanges);
  }

  public static BooleanExpr matchSrcProtocol(Set<Protocol> srcProtocols) {
    return matchProtocol(srcProtocols, true, false);
  }

  public static BooleanExpr matchState(Set<FlowState> states) {
    return new OrExpr(
        states
            .stream()
            .map(
                state ->
                    new EqExpr(
                        new VarIntExpr(Field.STATE),
                        new LitIntExpr(state.number(), Field.STATE.getSize())))
            .collect(ImmutableList.toImmutableList()));
  }

  public static BooleanExpr matchTcpFlags(List<TcpFlagsMatchConditions> tcpFlags) {
    LitIntExpr one = new LitIntExpr(1, 1);
    LitIntExpr zero = new LitIntExpr(0, 1);
    return new OrExpr(
        tcpFlags
            .stream()
            .map(
                currentTcpFlags -> {
                  ImmutableList.Builder<BooleanExpr> matchCurrentTcpFlags = ImmutableList.builder();
                  if (currentTcpFlags.getUseCwr()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getCwr() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_CWR), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseEce()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getEce() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_ECE), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseUrg()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getUrg() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_URG), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseAck()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getAck() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_ACK), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUsePsh()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getPsh() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_PSH), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseRst()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getRst() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_RST), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseSyn()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getSyn() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_SYN), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  if (currentTcpFlags.getUseFin()) {
                    LitIntExpr bit = currentTcpFlags.getTcpFlags().getFin() ? one : zero;
                    EqExpr matchFlag = new EqExpr(new VarIntExpr(Field.TCP_FLAGS_FIN), bit);
                    matchCurrentTcpFlags.add(matchFlag);
                  }
                  return new AndExpr(matchCurrentTcpFlags.build());
                })
            .collect(ImmutableList.toImmutableList()));
  }

  private final BooleanExpr _expr;

  private final Map<String, IpSpace> _namedIpSpaces;

  public HeaderSpaceMatchExpr(HeaderSpace headerSpace, Map<String, IpSpace> namedIpSpaces) {
    this(headerSpace, namedIpSpaces, false);
  }

  public HeaderSpaceMatchExpr(
      HeaderSpace headerSpace, Map<String, IpSpace> namedIpSpaces, boolean orig) {
    _namedIpSpaces = namedIpSpaces;
    ImmutableList.Builder<BooleanExpr> matchBuilder = ImmutableList.builder();

    // ipProtocol
    requireMatch(matchBuilder, headerSpace.getIpProtocols(), HeaderSpaceMatchExpr::matchIpProtocol);
    requireNoMatch(
        matchBuilder, headerSpace.getNotIpProtocols(), HeaderSpaceMatchExpr::matchIpProtocol);

    // protocol
    requireMatch(
        matchBuilder, headerSpace.getDstProtocols(), HeaderSpaceMatchExpr::matchDstProtocol);
    requireNoMatch(
        matchBuilder, headerSpace.getNotDstProtocols(), HeaderSpaceMatchExpr::matchDstProtocol);
    requireMatch(
        matchBuilder, headerSpace.getSrcProtocols(), HeaderSpaceMatchExpr::matchSrcProtocol);
    requireNoMatch(
        matchBuilder, headerSpace.getNotSrcProtocols(), HeaderSpaceMatchExpr::matchSrcProtocol);
    requireMatch(
        matchBuilder,
        headerSpace.getSrcOrDstProtocols(),
        HeaderSpaceMatchExpr::matchSrcOrDstProtocol);

    // ip
    if (headerSpace.getSrcIps() != null) {
      requireMatch(
          matchBuilder, headerSpace.getSrcIps(), orig ? this::matchOrigSrcIp : this::matchSrcIp);
    }
    if (headerSpace.getNotSrcIps() != null) {
      requireNoMatch(
          matchBuilder, headerSpace.getNotSrcIps(), orig ? this::matchOrigSrcIp : this::matchSrcIp);
    }
    if (headerSpace.getSrcOrDstIps() != null) {
      requireMatch(
          matchBuilder,
          headerSpace.getSrcOrDstIps(),
          orig ? this::matchOrigSrcOrDstIp : this::matchSrcOrDstIp);
    }
    if (headerSpace.getDstIps() != null) {
      requireMatch(matchBuilder, headerSpace.getDstIps(), this::matchDstIp);
    }
    if (headerSpace.getNotDstIps() != null) {
      requireNoMatch(matchBuilder, headerSpace.getNotDstIps(), this::matchDstIp);
    }

    // port
    requireMatch(matchBuilder, headerSpace.getSrcPorts(), HeaderSpaceMatchExpr::matchSrcPort);
    requireNoMatch(matchBuilder, headerSpace.getNotSrcPorts(), HeaderSpaceMatchExpr::matchSrcPort);
    requireMatch(
        matchBuilder, headerSpace.getSrcOrDstPorts(), HeaderSpaceMatchExpr::matchSrcOrDstPort);
    requireMatch(matchBuilder, headerSpace.getDstPorts(), HeaderSpaceMatchExpr::matchDstPort);
    requireNoMatch(matchBuilder, headerSpace.getNotDstPorts(), HeaderSpaceMatchExpr::matchDstPort);

    // dscp
    requireMatch(matchBuilder, headerSpace.getDscps(), HeaderSpaceMatchExpr::matchDscp);
    requireNoMatch(matchBuilder, headerSpace.getNotDscps(), HeaderSpaceMatchExpr::matchDscp);

    // ecn
    requireMatch(matchBuilder, headerSpace.getEcns(), HeaderSpaceMatchExpr::matchEcn);
    requireNoMatch(matchBuilder, headerSpace.getNotEcns(), HeaderSpaceMatchExpr::matchEcn);

    // fragmentOffset
    requireMatch(
        matchBuilder, headerSpace.getFragmentOffsets(), HeaderSpaceMatchExpr::matchFragmentOffset);
    requireNoMatch(
        matchBuilder,
        headerSpace.getNotFragmentOffsets(),
        HeaderSpaceMatchExpr::matchFragmentOffset);

    // connection-tracking state
    requireMatch(matchBuilder, headerSpace.getStates(), HeaderSpaceMatchExpr::matchState);

    // icmpTypes
    requireMatch(matchBuilder, headerSpace.getIcmpTypes(), HeaderSpaceMatchExpr::matchIcmpType);
    requireNoMatch(
        matchBuilder, headerSpace.getNotIcmpTypes(), HeaderSpaceMatchExpr::matchIcmpType);

    // icmpCodes
    requireMatch(matchBuilder, headerSpace.getIcmpCodes(), HeaderSpaceMatchExpr::matchIcmpCode);
    requireNoMatch(
        matchBuilder, headerSpace.getNotIcmpCodes(), HeaderSpaceMatchExpr::matchIcmpCode);

    // packetLengths
    requireMatch(
        matchBuilder, headerSpace.getPacketLengths(), HeaderSpaceMatchExpr::matchPacketLength);
    requireNoMatch(
        matchBuilder, headerSpace.getNotPacketLengths(), HeaderSpaceMatchExpr::matchPacketLength);

    // tcp-flags
    requireMatch(matchBuilder, headerSpace.getTcpFlags(), HeaderSpaceMatchExpr::matchTcpFlags);

    BooleanExpr match = new AndExpr(matchBuilder.build());
    if (headerSpace.getNegate()) {
      _expr = new NotExpr(match);
    } else {
      _expr = match;
    }
  }

  /**
   * Require that either ORIG_SRC_IP or DST_IP matches the input {@link IpSpace}. If the query
   * specifies a srcIp or dstIp constraint, we interpret that as meaning the srcIp before any
   * natting. If that constraint is imposed after src natting has been applied, use this method to
   * constrain ORIG_SRC_IP.
   */
  public BooleanExpr matchOrigSrcOrDstIp(IpSpace ipSpace) {
    return ipSpace.accept(
        new IpSpaceBooleanExprTransformer(_namedIpSpaces, Field.ORIG_SRC_IP, Field.DST_IP));
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitHeaderSpaceMatchExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitHeaderSpaceMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_expr, ((HeaderSpaceMatchExpr) e)._expr);
  }

  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }

  private void requireMatch(
      Builder<BooleanExpr> match, IpSpace ipSpace, Function<IpSpace, BooleanExpr> generator) {
    match.add(generator.apply(ipSpace));
  }

  private void requireNoMatch(
      Builder<BooleanExpr> match, IpSpace ipSpace, Function<IpSpace, BooleanExpr> generator) {
    match.add(new NotExpr(generator.apply(ipSpace)));
  }

  private <T, C extends Collection<T>> void requireMatch(
      Builder<BooleanExpr> match, C collection, Function<C, BooleanExpr> generator) {
    if (!collection.isEmpty()) {
      match.add(generator.apply(collection));
    }
  }

  private <T, C extends Collection<T>> void requireNoMatch(
      Builder<BooleanExpr> match, C collection, Function<C, BooleanExpr> generator) {
    if (!collection.isEmpty()) {
      match.add(new NotExpr(generator.apply(collection)));
    }
  }
}
