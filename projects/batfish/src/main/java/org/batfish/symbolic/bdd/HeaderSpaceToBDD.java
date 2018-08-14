package org.batfish.symbolic.bdd;

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
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

/** Convert a {@link HeaderSpace headerspace constraint} to a {@link BDD}. */
public final class HeaderSpaceToBDD {
  private final BDDFactory _bddFactory;
  private final BDDOps _bddOps;
  private final BDDPacket _bddPacket;
  private final Map<String, IpSpace> _namedIpSpaces;

  public HeaderSpaceToBDD(BDDPacket bddPacket, Map<String, IpSpace> namedIpSpaces) {
    _bddFactory = bddPacket.getFactory();
    _bddOps = new BDDOps(_bddFactory);
    _bddPacket = bddPacket;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
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
  BDD toBDD(@Nullable IpSpace ipSpace, BDDInteger var) {
    if (ipSpace == null) {
      return null;
    }
    return ipSpace.accept(new IpSpaceToBDD(_bddFactory, var, _namedIpSpaces));
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
  BDD toBDD(List<TcpFlags> tcpFlags) {
    if (tcpFlags == null || tcpFlags.isEmpty()) {
      return null;
    }

    return _bddOps.or(tcpFlags.stream().map(this::toBDD).collect(ImmutableList.toImmutableList()));
  }

  /** For TcpFlags */
  @VisibleForTesting
  static BDD toBDD(boolean useFlag, boolean flagValue, BDD flagBDD) {
    return useFlag ? flagValue ? flagBDD : flagBDD.not() : null;
  }

  @VisibleForTesting
  BDD toBDD(TcpFlags tcpFlags) {
    return _bddOps.and(
        toBDD(tcpFlags.getUseAck(), tcpFlags.getAck(), _bddPacket.getTcpAck()),
        toBDD(tcpFlags.getUseCwr(), tcpFlags.getCwr(), _bddPacket.getTcpCwr()),
        toBDD(tcpFlags.getUseEce(), tcpFlags.getEce(), _bddPacket.getTcpEce()),
        toBDD(tcpFlags.getUseFin(), tcpFlags.getFin(), _bddPacket.getTcpFin()),
        toBDD(tcpFlags.getUsePsh(), tcpFlags.getPsh(), _bddPacket.getTcpPsh()),
        toBDD(tcpFlags.getUseRst(), tcpFlags.getRst(), _bddPacket.getTcpRst()),
        toBDD(tcpFlags.getUseSyn(), tcpFlags.getSyn(), _bddPacket.getTcpSyn()),
        toBDD(tcpFlags.getUseUrg(), tcpFlags.getUrg(), _bddPacket.getTcpUrg()));
  }

  public BDD toBDD(HeaderSpace headerSpace) {
    return _bddOps.and(
        toBDD(headerSpace.getDstIps(), _bddPacket.getDstIp()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDstIps(), _bddPacket.getDstIp())),
        toBDD(headerSpace.getSrcIps(), _bddPacket.getSrcIp()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotSrcIps(), _bddPacket.getSrcIp())),
        BDDOps.orNull(
            toBDD(headerSpace.getSrcOrDstIps(), _bddPacket.getDstIp()),
            toBDD(headerSpace.getSrcOrDstIps(), _bddPacket.getSrcIp())),
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
            headerSpace.getStates().stream().map(State::number).collect(Collectors.toList()),
            _bddPacket.getState()));
  }
}
