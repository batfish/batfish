package org.batfish.z3;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDOps;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.symbolic.bdd.BDDSourceManager;
import org.batfish.symbolic.bdd.IpSpaceToBDD;

public final class BDDIpAccessListSpecializer extends IpAccessListSpecializer {
  private BDDSourceManager _bddSrcManager;
  private final BDDIpSpaceSpecializer _dstIpSpaceSpecializer;
  private final BDD _flowBDD;
  private final BDDPacket _pkt;
  private final BDDIpSpaceSpecializer _srcIpSpaceSpecializer;

  public BDDIpAccessListSpecializer(
      BDDPacket pkt,
      BDD flowBDD,
      Map<String, IpSpace> namedIpSpaces,
      BDDSourceManager bddSrcManager) {
    _bddSrcManager = bddSrcManager;
    _dstIpSpaceSpecializer =
        new BDDIpSpaceSpecializer(
            flowBDD,
            namedIpSpaces,
            new IpSpaceToBDD(pkt.getDstIp().getFactory(), pkt.getDstIp(), namedIpSpaces));
    _pkt = pkt;
    _flowBDD = flowBDD;
    _srcIpSpaceSpecializer =
        new BDDIpSpaceSpecializer(
            flowBDD,
            namedIpSpaces,
            new IpSpaceToBDD(pkt.getSrcIp().getFactory(), pkt.getSrcIp(), namedIpSpaces));
  }

  @VisibleForTesting
  public BDDIpAccessListSpecializer(
      BDDPacket pkt, BDD flowBDD, Map<String, IpSpace> namedIpSpaces) {
    this(pkt, flowBDD, namedIpSpaces, BDDSourceManager.forInterfaces(pkt, ImmutableSet.of()));
  }

  @Override
  boolean canSpecialize() {
    // _flowBDD.isOne means there is no constraint.
    return !_flowBDD.isOne();
  }

  @Override
  public HeaderSpace specialize(HeaderSpace headerSpace) {
    return headerSpace
        .toBuilder()
        // combine dstIps and notDstIps into dstIps
        .setDstIps(specializeIpSpace(headerSpace.getDstIps(), _dstIpSpaceSpecializer))
        .setNotDstIps(specializeIpSpace(headerSpace.getNotDstIps(), _dstIpSpaceSpecializer))
        .setDstPorts(specializeSubRange(headerSpace.getDstPorts(), _pkt.getDstPort()))
        .setNotDstPorts(specializeSubRange(headerSpace.getNotDstPorts(), _pkt.getDstPort()))
        .setIpProtocols(specializeIpProtocols(headerSpace.getIpProtocols()))
        .setIcmpCodes(specializeSubRange(headerSpace.getIcmpCodes(), _pkt.getIcmpCode()))
        .setIcmpTypes(specializeSubRange(headerSpace.getIcmpTypes(), _pkt.getIcmpType()))
        .setSrcOrDstIps(
            specializeIpSpace(
                headerSpace.getSrcOrDstIps(), _dstIpSpaceSpecializer, _srcIpSpaceSpecializer))
        .setSrcOrDstPorts(
            specializeSubRange(
                headerSpace.getSrcOrDstPorts(), _pkt.getSrcPort(), _pkt.getDstPort()))
        .setSrcPorts(specializeSubRange(headerSpace.getSrcPorts(), _pkt.getSrcPort()))
        .setNotSrcPorts(specializeSubRange(headerSpace.getNotSrcPorts(), _pkt.getSrcPort()))
        .setSrcIps(specializeIpSpace(headerSpace.getSrcIps(), _srcIpSpaceSpecializer))
        .setNotSrcIps(specializeIpSpace(headerSpace.getNotSrcIps(), _srcIpSpaceSpecializer))
        .setTcpFlags(specializeTcpFlags(headerSpace.getTcpFlags()))
        .build();
  }

  @Override
  public final AclLineMatchExpr visitOriginatingFromDevice(
      OriginatingFromDevice originatingFromDevice) {
    if (_flowBDD.imp(_bddSrcManager.getOriginatingFromDeviceBDD()).isOne()) {
      return TrueExpr.INSTANCE;
    } else if (_flowBDD.imp(_bddSrcManager.getOriginatingFromDeviceBDD().not()).isOne()) {
      return FalseExpr.INSTANCE;
    }
    return originatingFromDevice;
  }

  @Override
  public final AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    // Remove interfaces that can't match

    List<String> relevantInterfaces = new ArrayList<>();
    List<BDD> matchingInterfaceBDDs = new ArrayList<>();
    for (String iface : matchSrcInterface.getSrcInterfaces()) {
      BDD interfaceBDD = _bddSrcManager.getSourceInterfaceBDD(iface);
      if (!interfaceBDD.and(_flowBDD).isZero()) {
        matchingInterfaceBDDs.add(interfaceBDD);
        relevantInterfaces.add(iface);
      }
    }

    if (relevantInterfaces.isEmpty()) {
      return FalseExpr.INSTANCE;
    } else if (_flowBDD.imp(BDDOps.orNull(matchingInterfaceBDDs)).isOne()) {
      return TrueExpr.INSTANCE;
    }
    return new MatchSrcInterface(relevantInterfaces);
  }

  private @Nullable static IpSpace specializeIpSpace(
      @Nullable IpSpace ipSpace, BDDIpSpaceSpecializer... specializers) {
    if (ipSpace == null) {
      return null;
    }
    // union the subspaces relevant to each specializer
    List<IpSpace> specializedIpSpaces =
        Arrays.stream(specializers)
            .map(specializer -> specializer.specialize(ipSpace))
            .collect(Collectors.toList());
    return specializedIpSpaces.contains(UniverseIpSpace.INSTANCE)
        ? UniverseIpSpace.INSTANCE
        : AclIpSpace.union(specializedIpSpaces);
  }

  private @Nullable SortedSet<IpProtocol> specializeIpProtocols(
      @Nullable SortedSet<IpProtocol> ipProtocols) {
    return ipProtocols == null
        ? null
        : ipProtocols
            .stream()
            .filter(
                ipProtocol ->
                    !_flowBDD.and(_pkt.getIpProtocol().value(ipProtocol.number())).isZero())
            .collect(ImmutableSortedSet.toImmutableSortedSet(ipProtocols.comparator()));
  }

  private @Nullable SortedSet<SubRange> specializeSubRange(
      @Nullable SortedSet<SubRange> subRanges, BDDInteger... vars) {
    List<BDDInteger> varList = Arrays.asList(vars);
    return subRanges == null
        ? null
        : subRanges
            .stream()
            .filter(
                subRange ->
                    /*
                     * If none of the vars can match this subRange, remove it.
                     */
                    varList.stream().anyMatch(var -> !_flowBDD.and(toBDD(subRange, var)).isZero()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(subRanges.comparator()));
  }

  private @Nullable IpSpace specializeSrcIps(@Nullable IpSpace srcIps) {
    return srcIps == null ? null : _srcIpSpaceSpecializer.specialize(srcIps);
  }

  private @Nullable List<TcpFlags> specializeTcpFlags(@Nullable List<TcpFlags> tcpFlags) {
    return tcpFlags == null
        ? null
        : tcpFlags
            .stream()
            .filter(
                flags -> {
                  BDD flagsBDD =
                      BDDOps.andNull(
                          tcpFlagBDD(flags.getUseAck(), flags.getAck(), _pkt.getTcpAck()),
                          tcpFlagBDD(flags.getUseCwr(), flags.getCwr(), _pkt.getTcpCwr()),
                          tcpFlagBDD(flags.getUseEce(), flags.getEce(), _pkt.getTcpEce()),
                          tcpFlagBDD(flags.getUseFin(), flags.getFin(), _pkt.getTcpFin()),
                          tcpFlagBDD(flags.getUsePsh(), flags.getPsh(), _pkt.getTcpPsh()),
                          tcpFlagBDD(flags.getUseRst(), flags.getRst(), _pkt.getTcpRst()),
                          tcpFlagBDD(flags.getUseUrg(), flags.getUrg(), _pkt.getTcpUrg()));
                  return flagsBDD != null && !_flowBDD.and(flagsBDD).isZero();
                })
            .collect(ImmutableList.toImmutableList());
  }

  private @Nullable static BDD tcpFlagBDD(boolean use, boolean value, BDD bitBDD) {
    if (!use) {
      return null;
    }
    return value ? bitBDD : bitBDD.not();
  }

  private static BDD toBDD(SubRange subRange, BDDInteger var) {
    return var.geq(subRange.getStart()).and(var.leq(subRange.getEnd()));
  }
}
