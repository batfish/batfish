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
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDIpSpaceSpecializer;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * An {@link IpAccessListSpecializer} that uses {@link BDD BDDs} to represent the headerspace of
 * interest. The intuition of specialization is to remove parts of the ACL that are irrelevant for
 * the headerspace of interest.
 *
 * <p>The superclass {@link IpAccessListSpecializer} walks over the {@link
 * org.batfish.datamodel.IpAccessList ACL} and its {@link AclLineMatchExpr match expressions},
 * downcalling at the match expression leaves. For each leaf, we compute the space matched by that
 * expression (as a BDD) and compare with the space of interest. There are three cases:
 *
 * <ol>
 *   <li>The two spaces are disjoint. Then the expression is not relevant to the space of interest,
 *       and will be replaced by the {@code FalseExpr.INSTANCE}.
 *   <li>The match expression space contains the space of interest. Then the expression is relevant,
 *       but we also know that it will always evaluate to {@code true}. We can optionally optimize
 *       such expressions by replacing them with {@code TrueExpr.INSTANCE}.
 *   <li>The match expression intersects, but does not contain the space of interest. We preserve
 *       the expression as-is.
 * </ol>
 */
public final class BDDIpAccessListSpecializer extends IpAccessListSpecializer {
  private final BDDSourceManager _bddSrcManager;
  private final BDDIpSpaceSpecializer _dstIpSpaceSpecializer;
  private final BDD _flowBDD;
  private final BDDPacket _pkt;
  private final BDDIpSpaceSpecializer _srcIpSpaceSpecializer;
  private final boolean _simplifyToTrue;

  public BDDIpAccessListSpecializer(
      BDDPacket pkt,
      BDD flowBDD,
      Map<String, IpSpace> namedIpSpaces,
      BDDSourceManager bddSrcManager) {
    this(pkt, flowBDD, namedIpSpaces, bddSrcManager, true);
  }

  /**
   * @param pkt The {@link BDD} variables representing a symbolic packet.
   * @param flowBDD The representation of the headerspace of interest.
   * @param namedIpSpaces Definitions of named {@link IpSpace IpSpaces} currently in scope.
   * @param bddSrcManager Manages constraints for {@link OriginatingFromDevice} and {@link
   *     MatchSrcInterface} expressions.
   * @param simplifyToTrue Indicates whether expressions that are always true within the headerspace
   *     of interest (i.e. the match a space that includes the space of interest) should be
   *     optimized to the constant {@link TrueExpr}.
   */
  public BDDIpAccessListSpecializer(
      BDDPacket pkt,
      BDD flowBDD,
      Map<String, IpSpace> namedIpSpaces,
      BDDSourceManager bddSrcManager,
      boolean simplifyToTrue) {
    this(
        pkt,
        flowBDD,
        namedIpSpaces,
        bddSrcManager,
        new IpSpaceToBDD(pkt.getDstIp(), namedIpSpaces),
        new IpSpaceToBDD(pkt.getSrcIp(), namedIpSpaces),
        simplifyToTrue);
  }

  public BDDIpAccessListSpecializer(
      BDDPacket pkt,
      BDD flowBDD,
      Map<String, IpSpace> namedIpSpaces,
      BDDSourceManager bddSrcManager,
      IpSpaceToBDD dstIpSpaceToBdd,
      IpSpaceToBDD srcIpSpaceToBdd,
      boolean simplifyToTrue) {
    _bddSrcManager = bddSrcManager;
    _dstIpSpaceSpecializer =
        new BDDIpSpaceSpecializer(flowBDD, namedIpSpaces, dstIpSpaceToBdd, simplifyToTrue);
    _pkt = pkt;
    _flowBDD = flowBDD;
    _simplifyToTrue = simplifyToTrue;
    _srcIpSpaceSpecializer =
        new BDDIpSpaceSpecializer(flowBDD, namedIpSpaces, srcIpSpaceToBdd, simplifyToTrue);
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
    if (_simplifyToTrue && _flowBDD.imp(_bddSrcManager.getOriginatingFromDeviceBDD()).isOne()) {
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
    } else if (_simplifyToTrue && _flowBDD.imp(BDDOps.orNull(matchingInterfaceBDDs)).isOne()) {
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

  private @Nullable List<TcpFlagsMatchConditions> specializeTcpFlags(
      @Nullable List<TcpFlagsMatchConditions> tcpFlags) {
    return tcpFlags == null
        ? null
        : tcpFlags
            .stream()
            .filter(
                flags -> {
                  BDD flagsBDD =
                      BDDOps.andNull(
                          tcpFlagBDD(
                              flags.getUseAck(), flags.getTcpFlags().getAck(), _pkt.getTcpAck()),
                          tcpFlagBDD(
                              flags.getUseCwr(), flags.getTcpFlags().getCwr(), _pkt.getTcpCwr()),
                          tcpFlagBDD(
                              flags.getUseEce(), flags.getTcpFlags().getEce(), _pkt.getTcpEce()),
                          tcpFlagBDD(
                              flags.getUseFin(), flags.getTcpFlags().getFin(), _pkt.getTcpFin()),
                          tcpFlagBDD(
                              flags.getUsePsh(), flags.getTcpFlags().getPsh(), _pkt.getTcpPsh()),
                          tcpFlagBDD(
                              flags.getUseRst(), flags.getTcpFlags().getRst(), _pkt.getTcpRst()),
                          tcpFlagBDD(
                              flags.getUseUrg(), flags.getTcpFlags().getUrg(), _pkt.getTcpUrg()));
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
