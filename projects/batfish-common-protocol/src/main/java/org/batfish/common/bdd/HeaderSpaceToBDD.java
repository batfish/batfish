package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;

/** Convert a {@link HeaderSpace headerspace constraint} to a {@link BDD}. */
public final class HeaderSpaceToBDD {
  private final BDDFactory _bddFactory;
  private final BDDOps _bddOps;
  private final BDDPacket _bddPacket;
  private final IpSpaceToBDD _dstIpSpaceToBdd;
  private final IpSpaceToBDD _srcIpSpaceToBdd;

  public HeaderSpaceToBDD(BDDPacket bddPacket, Map<String, IpSpace> namedIpSpaces) {
    _bddFactory = bddPacket.getFactory();
    _bddOps = new BDDOps(_bddFactory);
    _bddPacket = bddPacket;
    _dstIpSpaceToBdd =
        namedIpSpaces.isEmpty()
            ? bddPacket.getDstIpSpaceToBDD()
            : new IpSpaceToBDD(bddPacket.getDstIpSpaceToBDD(), namedIpSpaces);
    _srcIpSpaceToBdd =
        namedIpSpaces.isEmpty()
            ? bddPacket.getSrcIpSpaceToBDD()
            : new IpSpaceToBDD(bddPacket.getSrcIpSpaceToBDD(), namedIpSpaces);
  }

  /** Returns bdd.not() or {@code null} if given {@link BDD} is null. */
  private static @Nullable BDD negateIfNonNull(BDD bdd) {
    return bdd == null ? bdd : bdd.not();
  }

  /**
   * A variant of {@link BDDOps#or(BDD...)} that returns {@code null} when all disjuncts are null.
   */
  @VisibleForTesting
  static BDD orNull(BDD... disjuncts) {
    BDD result = null;
    for (BDD disjunct : disjuncts) {
      if (disjunct != null) {
        result = result == null ? disjunct : result.or(disjunct);
      }
    }
    return result;
  }

  public IpSpaceToBDD getDstIpSpaceToBdd() {
    return _dstIpSpaceToBdd;
  }

  public IpSpaceToBDD getSrcIpSpaceToBdd() {
    return _srcIpSpaceToBdd;
  }

  @Nullable
  private BDD toBDD(Collection<Integer> ints, BDDInteger var) {
    if (ints == null || ints.isEmpty()) {
      return null;
    }
    return _bddOps.or(ints.stream().map(var::value).collect(Collectors.toList()));
  }

  @VisibleForTesting
  @Nullable
  static BDD toBDD(@Nullable IpSpace ipSpace, IpSpaceToBDD toBdd) {
    return ipSpace == null ? null : toBdd.visit(ipSpace);
  }

  @Nullable
  private BDD toBDD(Set<IpProtocol> ipProtocols) {
    if (ipProtocols == null || ipProtocols.isEmpty()) {
      return null;
    }

    return _bddOps.or(
        ipProtocols.stream().map(_bddPacket.getIpProtocol()::value).collect(Collectors.toList()));
  }

  @Nullable
  private BDD toBDD(@Nullable Set<SubRange> ranges, BDDInteger var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _bddOps.or(
        ranges.stream().map(range -> toBDD(range, var)).collect(ImmutableList.toImmutableList()));
  }

  @Nullable
  private BDD toBDD(@Nullable Set<SubRange> ranges, BDDIcmpCode var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _bddOps.or(
        ranges.stream().map(range -> toBDD(range, var)).collect(ImmutableList.toImmutableList()));
  }

  @Nullable
  private BDD toBDD(@Nullable Set<SubRange> ranges, BDDIcmpType var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _bddOps.or(
        ranges.stream().map(range -> toBDD(range, var)).collect(ImmutableList.toImmutableList()));
  }

  private BDD toBDD(@Nullable Set<SubRange> packetLengths, BDDPacketLength packetLength) {
    if (packetLengths == null || packetLengths.isEmpty()) {
      return null;
    }

    return _bddOps.or(
        packetLengths.stream()
            .map(range -> toBDD(range, packetLength))
            .collect(ImmutableList.toImmutableList()));
  }

  private static BDD toBDD(SubRange range, BDDIcmpCode var) {
    int start = range.getStart();
    int end = range.getEnd();
    return start == end ? var.value(start) : var.geq(start).and(var.leq(end));
  }

  private static BDD toBDD(SubRange range, BDDIcmpType var) {
    int start = range.getStart();
    int end = range.getEnd();
    return start == end ? var.value(start) : var.geq(start).and(var.leq(end));
  }

  private static BDD toBDD(SubRange range, BDDPacketLength packetLength) {
    int start = range.getStart();
    int end = range.getEnd();
    return start == end ? packetLength.value(start) : packetLength.range(start, end);
  }

  private static BDD toBDD(SubRange range, BDDInteger var) {
    return var.range(range.getStart(), range.getEnd());
  }

  private BDD toBDD(List<TcpFlagsMatchConditions> tcpFlags) {
    if (tcpFlags == null || tcpFlags.isEmpty()) {
      return null;
    }

    return _bddOps.or(tcpFlags.stream().map(this::toBDD).collect(ImmutableList.toImmutableList()));
  }

  /** For TcpFlagsMatchConditions */
  private @Nonnull BDD toBDD(boolean useFlag, boolean flagValue, BDD flagBDD) {
    if (!useFlag) {
      return _bddFactory.one();
    }
    return flagValue ? flagBDD : flagBDD.not();
  }

  private @Nonnull BDD toBDD(TcpFlagsMatchConditions tcpFlags) {
    return _bddOps.and(
        toBDD(tcpFlags.getUseUrg(), tcpFlags.getTcpFlags().getUrg(), _bddPacket.getTcpUrg()),
        toBDD(tcpFlags.getUseSyn(), tcpFlags.getTcpFlags().getSyn(), _bddPacket.getTcpSyn()),
        toBDD(tcpFlags.getUseRst(), tcpFlags.getTcpFlags().getRst(), _bddPacket.getTcpRst()),
        toBDD(tcpFlags.getUsePsh(), tcpFlags.getTcpFlags().getPsh(), _bddPacket.getTcpPsh()),
        toBDD(tcpFlags.getUseFin(), tcpFlags.getTcpFlags().getFin(), _bddPacket.getTcpFin()),
        toBDD(tcpFlags.getUseEce(), tcpFlags.getTcpFlags().getEce(), _bddPacket.getTcpEce()),
        toBDD(tcpFlags.getUseCwr(), tcpFlags.getTcpFlags().getCwr(), _bddPacket.getTcpCwr()),
        toBDD(tcpFlags.getUseAck(), tcpFlags.getTcpFlags().getAck(), _bddPacket.getTcpAck()));
  }

  public BDD toBDD(HeaderSpace headerSpace) {
    // HeaderSpace has null fields to mean "unconstrained". Thus we must be careful to treat these
    // as correctly "everything" when positive or "nothing" when negated.
    BDD[] conjuncts =
        new BDD[] {
          toBDD(headerSpace.getPacketLengths(), _bddPacket.getPacketLength()),
          negateIfNonNull(toBDD(headerSpace.getNotPacketLengths(), _bddPacket.getPacketLength())),
          toBDD(headerSpace.getFragmentOffsets(), _bddPacket.getFragmentOffset()),
          negateIfNonNull(
              toBDD(headerSpace.getNotFragmentOffsets(), _bddPacket.getFragmentOffset())),
          toBDD(headerSpace.getEcns(), _bddPacket.getEcn()),
          negateIfNonNull(toBDD(headerSpace.getNotEcns(), _bddPacket.getEcn())),
          toBDD(headerSpace.getDscps(), _bddPacket.getDscp()),
          negateIfNonNull(toBDD(headerSpace.getNotDscps(), _bddPacket.getDscp())),
          toBDD(headerSpace.getTcpFlags()),
          toBDD(headerSpace.getIcmpTypes(), _bddPacket.getIcmpType()),
          negateIfNonNull(toBDD(headerSpace.getNotIcmpTypes(), _bddPacket.getIcmpType())),
          toBDD(headerSpace.getIcmpCodes(), _bddPacket.getIcmpCode()),
          negateIfNonNull(toBDD(headerSpace.getNotIcmpCodes(), _bddPacket.getIcmpCode())),
          toBDD(headerSpace.getIpProtocols()),
          negateIfNonNull(toBDD(headerSpace.getNotIpProtocols())),
          toBDD(headerSpace.getSrcPorts(), _bddPacket.getSrcPort()),
          negateIfNonNull(toBDD(headerSpace.getNotSrcPorts(), _bddPacket.getSrcPort())),
          orNull(
              toBDD(headerSpace.getSrcOrDstPorts(), _bddPacket.getSrcPort()),
              toBDD(headerSpace.getSrcOrDstPorts(), _bddPacket.getDstPort())),
          toBDD(headerSpace.getDstPorts(), _bddPacket.getDstPort()),
          negateIfNonNull(toBDD(headerSpace.getNotDstPorts(), _bddPacket.getDstPort())),
          toBDD(headerSpace.getSrcIps(), _srcIpSpaceToBdd),
          negateIfNonNull(toBDD(headerSpace.getNotSrcIps(), _srcIpSpaceToBdd)),
          orNull(
              toBDD(headerSpace.getSrcOrDstIps(), _srcIpSpaceToBdd),
              toBDD(headerSpace.getSrcOrDstIps(), _dstIpSpaceToBdd)),
          toBDD(headerSpace.getDstIps(), _dstIpSpaceToBdd),
          negateIfNonNull(toBDD(headerSpace.getNotDstIps(), _dstIpSpaceToBdd))
        };
    BDD positiveSpace =
        _bddOps.and(Arrays.stream(conjuncts).filter(Objects::nonNull).collect(Collectors.toList()));
    return headerSpace.getNegate() ? positiveSpace.not() : positiveSpace;
  }
}
