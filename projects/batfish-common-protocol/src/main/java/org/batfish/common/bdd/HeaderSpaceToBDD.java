package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.FlowState;
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
  private final Map<String, IpSpace> _namedIpSpaces;
  private final IpSpaceToBDD _dstIpSpaceToBdd;
  private final IpSpaceToBDD _srcIpSpaceToBdd;

  public HeaderSpaceToBDD(BDDPacket bddPacket, Map<String, IpSpace> namedIpSpaces) {
    _bddFactory = bddPacket.getFactory();
    _bddOps = new BDDOps(_bddFactory);
    _bddPacket = bddPacket;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _dstIpSpaceToBdd = new IpSpaceToBDD(_bddPacket.getDstIp(), _namedIpSpaces);
    _srcIpSpaceToBdd = new IpSpaceToBDD(_bddPacket.getSrcIp(), _namedIpSpaces);
  }

  public HeaderSpaceToBDD(
      BDDPacket bddPacket,
      Map<String, IpSpace> namedIpSpaces,
      IpSpaceToBDD dstIpSpaceToBdd,
      IpSpaceToBDD srcIpSpaceToBdd) {
    _bddFactory = bddPacket.getFactory();
    _bddOps = new BDDOps(_bddFactory);
    _bddPacket = bddPacket;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _dstIpSpaceToBdd = dstIpSpaceToBdd;
    _srcIpSpaceToBdd = srcIpSpaceToBdd;
  }

  public IpSpaceToBDD getDstIpSpaceToBdd() {
    return _dstIpSpaceToBdd;
  }

  public IpSpaceToBDD getSrcIpSpaceToBdd() {
    return _srcIpSpaceToBdd;
  }

  @VisibleForTesting
  @Nullable
  BDD toBDD(Collection<Integer> ints, BDDInteger var) {
    if (ints == null || ints.isEmpty()) {
      return null;
    }
    return _bddOps.or(ints.stream().map(var::value).collect(Collectors.toList()));
  }

  @VisibleForTesting
  @Nullable
  BDD toBDD(@Nullable IpSpace ipSpace, IpSpaceToBDD toBdd) {
    return ipSpace == null ? null : toBdd.visit(ipSpace);
  }

  @VisibleForTesting
  @Nullable
  BDD toBDD(Set<IpProtocol> ipProtocols) {
    if (ipProtocols == null || ipProtocols.isEmpty()) {
      return null;
    }

    return _bddOps.or(
        ipProtocols
            .stream()
            .map(IpProtocol::number)
            .map(_bddPacket.getIpProtocol()::value)
            .collect(Collectors.toList()));
  }

  @VisibleForTesting
  @Nullable
  BDD toBDD(@Nullable Set<SubRange> ranges, BDDInteger var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _bddOps.or(
        ranges.stream().map(range -> toBDD(range, var)).collect(ImmutableList.toImmutableList()));
  }

  @VisibleForTesting
  static BDD toBDD(SubRange range, BDDInteger var) {
    long start = range.getStart();
    long end = range.getEnd();
    return start == end ? var.value(start) : var.geq(start).and(var.leq(end));
  }

  @VisibleForTesting
  BDD toBDD(List<TcpFlagsMatchConditions> tcpFlags) {
    if (tcpFlags == null || tcpFlags.isEmpty()) {
      return null;
    }

    return _bddOps.or(tcpFlags.stream().map(this::toBDD).collect(ImmutableList.toImmutableList()));
  }

  /** For TcpFlagsMatchConditions */
  @VisibleForTesting
  static BDD toBDD(boolean useFlag, boolean flagValue, BDD flagBDD) {
    return useFlag ? flagValue ? flagBDD : flagBDD.not() : null;
  }

  @VisibleForTesting
  BDD toBDD(TcpFlagsMatchConditions tcpFlags) {
    return _bddOps.and(
        toBDD(tcpFlags.getUseAck(), tcpFlags.getTcpFlags().getAck(), _bddPacket.getTcpAck()),
        toBDD(tcpFlags.getUseCwr(), tcpFlags.getTcpFlags().getCwr(), _bddPacket.getTcpCwr()),
        toBDD(tcpFlags.getUseEce(), tcpFlags.getTcpFlags().getEce(), _bddPacket.getTcpEce()),
        toBDD(tcpFlags.getUseFin(), tcpFlags.getTcpFlags().getFin(), _bddPacket.getTcpFin()),
        toBDD(tcpFlags.getUsePsh(), tcpFlags.getTcpFlags().getPsh(), _bddPacket.getTcpPsh()),
        toBDD(tcpFlags.getUseRst(), tcpFlags.getTcpFlags().getRst(), _bddPacket.getTcpRst()),
        toBDD(tcpFlags.getUseSyn(), tcpFlags.getTcpFlags().getSyn(), _bddPacket.getTcpSyn()),
        toBDD(tcpFlags.getUseUrg(), tcpFlags.getTcpFlags().getUrg(), _bddPacket.getTcpUrg()));
  }

  public BDD toBDD(HeaderSpace headerSpace) {
    BDD positiveSpace =
        _bddOps.and(
            toBDD(headerSpace.getDstIps(), _dstIpSpaceToBdd),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDstIps(), _dstIpSpaceToBdd)),
            toBDD(headerSpace.getSrcIps(), _srcIpSpaceToBdd),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotSrcIps(), _srcIpSpaceToBdd)),
            BDDOps.orNull(
                toBDD(headerSpace.getSrcOrDstIps(), _dstIpSpaceToBdd),
                toBDD(headerSpace.getSrcOrDstIps(), _srcIpSpaceToBdd)),
            toBDD(headerSpace.getDstPorts(), _bddPacket.getDstPort()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDstPorts(), _bddPacket.getDstPort())),
            toBDD(headerSpace.getSrcPorts(), _bddPacket.getSrcPort()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotSrcPorts(), _bddPacket.getSrcPort())),
            BDDOps.orNull(
                toBDD(headerSpace.getSrcOrDstPorts(), _bddPacket.getSrcPort()),
                toBDD(headerSpace.getSrcOrDstPorts(), _bddPacket.getDstPort())),
            toBDD(headerSpace.getTcpFlags()),
            toBDD(headerSpace.getIcmpCodes(), _bddPacket.getIcmpCode()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIcmpCodes(), _bddPacket.getIcmpCode())),
            toBDD(headerSpace.getIcmpTypes(), _bddPacket.getIcmpType()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIcmpTypes(), _bddPacket.getIcmpType())),
            toBDD(headerSpace.getIpProtocols()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIpProtocols())),
            toBDD(headerSpace.getDscps(), _bddPacket.getDscp()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDscps(), _bddPacket.getDscp())),
            toBDD(headerSpace.getEcns(), _bddPacket.getEcn()),
            BDDOps.negateIfNonNull(toBDD(headerSpace.getNotEcns(), _bddPacket.getEcn())),
            toBDD(headerSpace.getFragmentOffsets(), _bddPacket.getFragmentOffset()),
            BDDOps.negateIfNonNull(
                toBDD(headerSpace.getNotFragmentOffsets(), _bddPacket.getFragmentOffset())),
            toBDD(
                headerSpace
                    .getStates()
                    .stream()
                    .map(FlowState::number)
                    .collect(Collectors.toList()),
                _bddPacket.getState()));
    return headerSpace.getNegate() ? positiveSpace.not() : positiveSpace;
  }
}
